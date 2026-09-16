package com.example.mealcase.feature.meals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.mealcase.R
import com.example.mealcase.core.model.MealSummary
import com.example.mealcase.core.ui.EmptyContent
import com.example.mealcase.core.ui.ErrorContent
import com.example.mealcase.core.ui.LoadingContent
import com.example.mealcase.core.ui.UiState
import com.example.mealcase.core.ui.navigationBarsBottomPadding
import com.example.mealcase.core.ui.theme.Organic

/** Every meal appears once in a single list, with its thumbnail and name together. */
@Composable
fun MealListScreen(
    onMealClick: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MealListViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Organic.Cream),
    ) {
        MealListNav(viewModel.area, onBack)

        when (val state = uiState) {
            UiState.Loading -> LoadingContent(Modifier.fillMaxSize())

            UiState.Empty -> EmptyContent(
                message = stringResource(R.string.meals_empty, viewModel.area),
                modifier = Modifier.fillMaxSize(),
            )

            is UiState.Error -> ErrorContent(state.error, viewModel::load, Modifier.fillMaxSize())
            is UiState.Success -> MealList(state.data, onMealClick, Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun MealListNav(
    area: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backLabel = stringResource(R.string.action_back)
    Row(
        modifier = modifier
            // The app targets SDK 35+, so the system draws edge-to-edge by default —
            // without this the back button sits under the status bar / camera cutout.
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White)
                .semantics { contentDescription = backLabel }
                .clickable(role = Role.Button, onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "←",
                style = MaterialTheme.typography.titleMedium,
                color = Organic.Ink,
                modifier = Modifier.clearAndSetSemantics { },
            )
        }
        Text(
            text = area,
            style = MaterialTheme.typography.displayMedium,
            color = Organic.Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun MealList(
    meals: List<MealSummary>,
    onMealClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 4.dp,
            bottom = navigationBarsBottomPadding(24.dp),
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(meals, key = { it.id }) { meal ->
            MealRow(meal = meal, onClick = { onMealClick(meal.id) })
        }
    }
}

@Composable
private fun MealRow(
    meal: MealSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(36.dp))
            .clip(RoundedCornerShape(36.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Organic.SageHigh.copy(alpha = 0.18f)),
        ) {
            AsyncImage(
                model = meal.thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Text(
            text = meal.name,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Organic.Ink,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = "›",
            style = MaterialTheme.typography.headlineMedium,
            color = Organic.InkMuted,
            modifier = Modifier.padding(end = 4.dp),
        )
    }
}
