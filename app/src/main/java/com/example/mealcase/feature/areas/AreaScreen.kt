package com.example.mealcase.feature.areas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mealcase.R
import com.example.mealcase.core.common.AppError
import com.example.mealcase.core.model.Area
import com.example.mealcase.core.model.AreaDiscovery
import com.example.mealcase.core.ui.ErrorContent
import com.example.mealcase.core.ui.LoadingContent
import com.example.mealcase.core.ui.UiState
import com.example.mealcase.core.ui.navigationBarsBottomPadding
import com.example.mealcase.core.ui.theme.Organic

/**
 * Areas screen, "bold editorial" style: a full-bleed terracotta header carrying an oversized
 * title, with the cream content sheet rounding up over it and holding a flush-left list of
 * cuisines — no cards, no icons, just type and thin dividers.
 */
@Composable
fun AreaScreen(
    onAreaClick: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AreaViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Organic.Terracotta),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // The app targets SDK 35+, so the system draws edge-to-edge by default —
                // without this the title sits under the status bar / camera cutout.
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 32.dp),
        ) {
            Text(
                text = stringResource(R.string.areas_title),
                style = MaterialTheme.typography.displayLarge,
                color = Organic.CreamHigh,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Organic.Cream),
        ) {
            when (val state = uiState) {
                UiState.Loading -> LoadingContent(Modifier.fillMaxSize())
                UiState.Empty -> ErrorContent(AppError.Server, viewModel::load, Modifier.fillMaxSize())
                is UiState.Error -> ErrorContent(state.error, viewModel::load, Modifier.fillMaxSize())
                is UiState.Success -> AreaList(
                    discovery = state.data,
                    onAreaClick = onAreaClick,
                    onRetry = viewModel::load,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun AreaList(
    discovery: AreaDiscovery,
    onAreaClick: (String, String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        // Bottom padding clears the gesture pill / 3-button nav bar the same edge-to-edge
        // default draws underneath, so the last row's arrow isn't clipped by it.
        contentPadding = PaddingValues(top = 24.dp, bottom = navigationBarsBottomPadding(24.dp)),
    ) {
        if (discovery.isPartial) {
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                    Text(
                        text = stringResource(R.string.areas_partial),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Organic.Ink,
                    )
                    TextButton(onClick = onRetry) {
                        Text(stringResource(R.string.action_retry))
                    }
                }
            }
        }
        item {
            Text(
                text = stringResource(R.string.areas_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = Organic.InkMuted,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            )
        }
        // The upstream API has emitted duplicate names; index keys cannot collide.
        itemsIndexed(discovery.areas, key = { index, _ -> index }) { _, area ->
            AreaRow(
                area = area,
                onClick = { onAreaClick(area.name, area.country ?: area.name) },
            )
            HorizontalDivider(color = Organic.Divider, thickness = 1.dp)
        }
    }
}

@Composable
private fun AreaRow(
    area: Area,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = area.name,
            style = MaterialTheme.typography.headlineSmall,
            color = Organic.Ink,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "→",
            style = MaterialTheme.typography.headlineSmall,
            color = Organic.InkMuted,
        )
    }
}
