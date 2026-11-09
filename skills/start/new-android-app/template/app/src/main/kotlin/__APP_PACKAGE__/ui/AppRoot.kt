package __APP_PACKAGE__.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import __APP_PACKAGE__.AppContainer
import __APP_PACKAGE__.Page
import __APP_PACKAGE__.R
import __APP_PACKAGE__.settings.SettingsState
import __APP_PACKAGE__.ui.theme.AppTheme
import kotlinx.coroutines.launch

/**
 * The whole app below the activity. Tests call this with their own container.
 * [startPage] and [darkTheme] exist for snapshots; the app leaves them alone.
 */
@Composable
fun AppRoot(
    container: AppContainer,
    startPage: Page = Page.All,
    darkTheme: Boolean? = null,
) {
    val settings by container.settings.state.collectAsStateWithLifecycle(initialValue = SettingsState())
    AppTheme(
        accent = settings.accent,
        dynamicColor = settings.dynamicColor,
        darkTheme = darkTheme ?: isSystemInDarkTheme(),
    ) {
        val viewModel: ItemsViewModel = viewModel(factory = ItemsViewModel.factory(container.repository))
        val backStack = rememberNavBackStack(Route.Tabs)
        var page by rememberSaveable { mutableStateOf(startPage) }
        val snackbar = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        val deletedText = stringResource(R.string.deleted)
        val undoText = stringResource(R.string.undo)

        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryProvider = entryProvider {
                    entry<Route.Tabs> {
                        AppShell(
                            page = page,
                            onPage = { page = it },
                            viewModel = viewModel,
                            settings = settings,
                            snackbarHostState = snackbar,
                            onOpen = { id -> backStack.add(Route.Detail(id)) },
                            onAdd = { scope.launch { backStack.add(Route.Detail(viewModel.add())) } },
                            onAccent = { accent -> scope.launch { container.settings.setAccent(accent) } },
                            onDynamicColor = { on -> scope.launch { container.settings.setDynamicColor(on) } },
                        )
                    }
                    entry<Route.Detail> { route ->
                        ItemDetailScreen(
                            id = route.id,
                            viewModel = viewModel,
                            onBack = { backStack.removeLastOrNull() },
                            onDelete = {
                                scope.launch {
                                    val removed = viewModel.delete(route.id)
                                    backStack.removeLastOrNull()
                                    if (removed != null) {
                                        val result = snackbar.showSnackbar(
                                            message = deletedText,
                                            actionLabel = undoText,
                                        )
                                        if (result == SnackbarResult.ActionPerformed) viewModel.restore(removed)
                                    }
                                }
                            },
                        )
                    }
                },
            )
        }
    }
}
