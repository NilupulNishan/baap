@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)

package __APP_PACKAGE__.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import __APP_PACKAGE__.Page
import __APP_PACKAGE__.R
import __APP_PACKAGE__.settings.SettingsState
import __APP_PACKAGE__.ui.theme.Accent

/**
 * The shell: a bottom bar on phones and a rail on wide screens, one top bar,
 * and the page that is selected.
 */
@Composable
fun AppShell(
    page: Page,
    onPage: (Page) -> Unit,
    viewModel: ItemsViewModel,
    settings: SettingsState,
    snackbarHostState: SnackbarHostState,
    onOpen: (Long) -> Unit,
    onAdd: () -> Unit,
    onAccent: (Accent) -> Unit,
    onDynamicColor: (Boolean) -> Unit,
) {
    NavigationSuiteScaffold(
        navigationSuiteItems = {
            Page.entries.forEach { entry ->
                item(
                    selected = entry == page,
                    onClick = { onPage(entry) },
                    icon = { Icon(painterResource(entry.icon), contentDescription = null) },
                    label = { Text(stringResource(entry.title)) },
                    modifier = Modifier.testTag(entry.tag),
                )
            }
        },
    ) {
        Scaffold(
            topBar = { TopAppBar(title = { Text(stringResource(page.title)) }) },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                if (page != Page.Settings) {
                    FloatingActionButton(onClick = onAdd) {
                        Icon(painterResource(R.drawable.ic_add), contentDescription = stringResource(R.string.add_item))
                    }
                }
            },
        ) { padding ->
            when (page) {
                Page.All -> {
                    val items by viewModel.all.collectAsStateWithLifecycle()
                    ItemsScreen(
                        items = items,
                        emptyTitle = R.string.empty_all_title,
                        emptyBody = R.string.empty_all_body,
                        emptyAction = R.string.empty_all_action,
                        onEmptyAction = onAdd,
                        onOpen = onOpen,
                        onToggleStar = { item -> viewModel.setStarred(item.id, !item.starred) },
                        modifier = Modifier.padding(padding),
                    )
                }
                Page.Starred -> {
                    val items by viewModel.starred.collectAsStateWithLifecycle()
                    ItemsScreen(
                        items = items,
                        emptyTitle = R.string.empty_starred_title,
                        emptyBody = R.string.empty_starred_body,
                        emptyAction = R.string.empty_starred_action,
                        onEmptyAction = { onPage(Page.All) },
                        onOpen = onOpen,
                        onToggleStar = { item -> viewModel.setStarred(item.id, !item.starred) },
                        modifier = Modifier.padding(padding),
                    )
                }
                Page.Settings -> SettingsScreen(
                    settings = settings,
                    onAccent = onAccent,
                    onDynamicColor = onDynamicColor,
                    modifier = Modifier.padding(padding),
                )
            }
        }
    }
}
