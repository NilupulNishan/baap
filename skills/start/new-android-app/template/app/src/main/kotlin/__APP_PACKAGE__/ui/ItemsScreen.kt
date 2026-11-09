package __APP_PACKAGE__.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import __APP_PACKAGE__.R
import __APP_PACKAGE__.data.Item
import __APP_PACKAGE__.lib.ItemTitle
import __APP_PACKAGE__.ui.theme.spacing

/** A list page. [items] is null while loading. */
@Composable
fun ItemsScreen(
    items: List<Item>?,
    @StringRes emptyTitle: Int,
    @StringRes emptyBody: Int,
    @StringRes emptyAction: Int,
    onEmptyAction: () -> Unit,
    onOpen: (Long) -> Unit,
    onToggleStar: (Item) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        items == null -> Box(modifier.fillMaxSize())
        items.isEmpty() -> EmptyState(
            title = emptyTitle,
            body = emptyBody,
            action = emptyAction,
            onAction = onEmptyAction,
            modifier = modifier,
        )
        else -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = MaterialTheme.spacing.fabClearance),
        ) {
            items(items, key = { it.id }, contentType = { "item" }) { item ->
                ItemRow(item, onOpen = { onOpen(item.id) }, onToggleStar = { onToggleStar(item) })
            }
        }
    }
}

@Composable
private fun ItemRow(item: Item, onOpen: () -> Unit, onToggleStar: () -> Unit) {
    val title = ItemTitle.clean(item.title).ifEmpty { stringResource(R.string.untitled) }
    ListItem(
        headlineContent = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        trailingContent = {
            IconButton(onClick = onToggleStar) {
                Icon(
                    painter = painterResource(if (item.starred) R.drawable.ic_star else R.drawable.ic_star_outline),
                    contentDescription = stringResource(if (item.starred) R.string.unstar else R.string.star),
                    tint = if (item.starred) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        modifier = Modifier
            .clickable(onClick = onOpen)
            .gridProbe("items/row"),
    )
}

/** Says what the page is for and offers the first action as a button. */
@Composable
private fun EmptyState(
    @StringRes title: Int,
    @StringRes body: Int,
    @StringRes action: Int,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_list),
            contentDescription = null,
            modifier = Modifier.size(MaterialTheme.spacing.touch),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.l))
        Text(stringResource(title), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(MaterialTheme.spacing.xs))
        Text(
            stringResource(body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.xl))
        Button(onClick = onAction) { Text(stringResource(action)) }
    }
}
