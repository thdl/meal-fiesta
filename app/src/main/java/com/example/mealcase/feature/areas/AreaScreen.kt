package com.example.mealcase.feature.areas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mealcase.R
import com.example.mealcase.app.di.ViewModelFactories
import com.example.mealcase.core.model.Area
import com.example.mealcase.core.ui.EmptyContent
import com.example.mealcase.core.ui.ErrorContent
import com.example.mealcase.core.ui.LoadingContent
import com.example.mealcase.core.ui.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AreaScreen(
    onAreaClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AreaViewModel = viewModel(factory = ViewModelFactories.area),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.areas_title)) }) },
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        when (val state = uiState) {
            UiState.Loading -> LoadingContent(contentModifier)
            UiState.Empty -> EmptyContent(stringResource(R.string.error_server), contentModifier)
            is UiState.Error -> ErrorContent(state.error, viewModel::load, contentModifier)
            is UiState.Success -> AreaList(state.data, onAreaClick, contentModifier)
        }
    }
}

@Composable
private fun AreaList(
    areas: List<Area>,
    onAreaClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.areas_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }
        items(areas, key = { area -> "${area.name}:${area.country.orEmpty()}" }) { area ->
            AreaCard(area = area, onClick = { onAreaClick(area.name) })
        }
    }
}

@Composable
private fun AreaCard(
    area: Area,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = area.name, style = MaterialTheme.typography.titleMedium)
            // The API hands us the country name alongside the cuisine, so it costs nothing.
            // Emoji flags would need an ISO mapping for ~195 non-standard names — not worth it.
            if (area.country != null) {
                Text(
                    text = area.country,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
