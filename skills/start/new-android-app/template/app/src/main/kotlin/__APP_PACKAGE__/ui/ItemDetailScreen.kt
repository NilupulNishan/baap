@file:OptIn(ExperimentalMaterial3Api::class)

package __APP_PACKAGE__.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import __APP_PACKAGE__.R
import __APP_PACKAGE__.ui.theme.spacing

/** One item: rename it, star it, delete it. Saves as you type, so there is no Save button. */
@Composable
fun ItemDetailScreen(
    id: Long,
    viewModel: ItemsViewModel,
    onBack: () -> Unit,
    onDelete: () -> Unit,
) {
    val item by remember(id) { viewModel.item(id) }.collectAsStateWithLifecycle(initialValue = null)

    // The field edits a local copy, loaded once, so a database update never moves the cursor.
    var title by rememberSaveable(id) { mutableStateOf<String?>(null) }
    val loaded = item
    LaunchedEffect(loaded?.id) {
        if (title == null && loaded != null) title = loaded.title
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    val starred = loaded?.starred == true
                    IconButton(onClick = { viewModel.setStarred(id, !starred) }) {
                        Icon(
                            painter = painterResource(if (starred) R.drawable.ic_star else R.drawable.ic_star_outline),
                            contentDescription = stringResource(if (starred) R.string.unstar else R.string.star),
                            tint = if (starred) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(painterResource(R.drawable.ic_delete), contentDescription = stringResource(R.string.delete))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = MaterialTheme.spacing.l),
        ) {
            val text = title
            if (text != null) {
                val focusRequester = remember { FocusRequester() }
                Box(modifier = Modifier.gridProbe("detail/title")) {
                    OutlinedTextField(
                        value = text,
                        onValueChange = {
                            title = it
                            viewModel.rename(id, it)
                        },
                        label = { Text(stringResource(R.string.title_label)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { onBack() }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .testTag("title-field"),
                    )
                }
                // A new item has no title yet, so the keyboard opens ready to type.
                LaunchedEffect(Unit) {
                    if (text.isEmpty()) focusRequester.requestFocus()
                }
            }
        }
    }
}
