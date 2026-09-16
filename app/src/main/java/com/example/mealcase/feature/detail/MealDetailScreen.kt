package com.example.mealcase.feature.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.mealcase.R
import com.example.mealcase.core.model.Ingredient
import com.example.mealcase.core.model.MealDetail
import com.example.mealcase.core.ui.EmptyContent
import com.example.mealcase.core.ui.ErrorContent
import com.example.mealcase.core.ui.LoadingContent
import com.example.mealcase.core.ui.UiState
import com.example.mealcase.core.ui.navigationBarsBottomPadding
import com.example.mealcase.core.ui.theme.Organic

/**
 * Meal detail, "bold editorial" style: a full-bleed gradient hero carrying the name, then a
 * two-tab switch between ingredients and instructions — both rendered as plain flush-left
 * lists, no card container.
 */
@Composable
fun MealDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MealDetailViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val state = uiState

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Organic.Cream),
    ) {
        when (state) {
            UiState.Loading -> LoadingContent(Modifier.fillMaxSize())
            UiState.Empty -> EmptyContent(stringResource(R.string.error_not_found), Modifier.fillMaxSize())
            is UiState.Error -> ErrorContent(state.error, viewModel::load, Modifier.fillMaxSize())
            is UiState.Success -> MealDetailContent(state.data, Modifier.fillMaxSize())
        }

        BackButton(onBack, modifier = Modifier.align(Alignment.TopStart))
    }
}

@Composable
private fun BackButton(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backLabel = stringResource(R.string.action_back)
    Box(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(16.dp)
            .size(48.dp)
            .clip(CircleShape)
            .background(Organic.CreamHigh.copy(alpha = 0.9f))
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
}

@Composable
private fun MealDetailContent(
    meal: MealDetail,
    modifier: Modifier = Modifier,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
    ) {
        Hero(meal)

        TabBar(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
        )

        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
            if (selectedTab == 0) {
                IngredientsTab(meal.ingredients)
            } else {
                InstructionsTab(meal.instructions)
            }
        }

        // Content scrolls under the gesture pill / 3-button nav bar otherwise — the app
        // targets SDK 35+, where the system draws edge-to-edge by default.
        Spacer(Modifier.height(navigationBarsBottomPadding(24.dp)))
    }
}

@Composable
private fun Hero(
    meal: MealDetail,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(
                Brush.linearGradient(listOf(Organic.TerracottaHigh, Organic.SageHigh)),
            ),
    ) {
        if (meal.thumbnailUrl != null) {
            AsyncImage(
                model = meal.thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Text(
            text = meal.name,
            style = MaterialTheme.typography.displayMedium.copy(
                shadow = Shadow(color = Color.Black.copy(alpha = 0.6f), blurRadius = 16f),
            ),
            color = Color.White,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 24.dp, vertical = 20.dp),
        )
    }
}

@Composable
private fun TabBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = listOf(
        stringResource(R.string.detail_ingredients),
        stringResource(R.string.detail_instructions),
    )

    Column(modifier = modifier.background(Organic.Cream)) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            tabs.forEachIndexed { index, label ->
                val selected = index == selectedTab
                Column(
                    // Without this, the underline Box's fillMaxWidth() below measures against
                    // the Row's full width (Row gives unweighted children loose constraints up
                    // to its own max width) instead of this tab's own label width — which was
                    // squeezing the second tab down to a sliver so narrow its text wrapped
                    // into a tall single-character-wide stack, invisible and pushing this
                    // whole bar's height out to match. IntrinsicSize.Min makes the Column (and
                    // so the Row child) size to its content — the label's own width — first.
                    modifier = Modifier
                        .width(IntrinsicSize.Min)
                        .selectable(
                            selected = selected,
                            role = Role.Tab,
                            onClick = { onTabSelected(index) },
                        )
                        .padding(vertical = 16.dp),
                ) {
                    Text(
                        text = label,
                        style = if (selected) {
                            MaterialTheme.typography.titleMedium
                        } else {
                            MaterialTheme.typography.bodyLarge
                        },
                        color = if (selected) Organic.Ink else Organic.InkMuted,
                    )
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .height(2.dp)
                            .fillMaxWidth()
                            .background(if (selected) Organic.Terracotta600 else Color.Transparent),
                    )
                }
            }
        }
        HorizontalDivider(color = Organic.Divider, thickness = 1.dp)
    }
}

@Composable
private fun IngredientsTab(
    ingredients: List<Ingredient>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (ingredients.isEmpty()) {
            Text(
                text = stringResource(R.string.detail_no_ingredients),
                style = MaterialTheme.typography.bodyLarge,
                color = Organic.InkMuted,
                modifier = Modifier.padding(vertical = 16.dp),
            )
            return@Column
        }
        ingredients.forEach { ingredient ->
            IngredientRow(ingredient)
            HorizontalDivider(color = Organic.Divider, thickness = 1.dp)
        }
    }
}

@Composable
private fun IngredientRow(
    ingredient: Ingredient,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = ingredient.name,
            style = MaterialTheme.typography.bodyLarge,
            color = Organic.Ink,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = ingredient.measure,
            style = MaterialTheme.typography.bodyLarge,
            color = Organic.InkMuted,
        )
    }
}

@Composable
private fun InstructionsTab(
    instructions: String?,
    modifier: Modifier = Modifier,
) {
    Text(
        text = instructions ?: stringResource(R.string.detail_no_instructions),
        style = MaterialTheme.typography.bodyLarge,
        color = Organic.Ink,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
    )
}
