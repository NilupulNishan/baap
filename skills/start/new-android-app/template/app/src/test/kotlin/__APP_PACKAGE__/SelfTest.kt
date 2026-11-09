package __APP_PACKAGE__

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import __APP_PACKAGE__.ui.AppRoot
import __APP_PACKAGE__.ui.theme.Accent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import kotlin.io.path.createTempDirectory

/** Waits for Room and DataStore, which answer on their own threads. */
fun ComposeContentTestRule.waitForText(text: String, timeoutMillis: Long = 5_000) {
    waitUntil(timeoutMillis) { onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
}

fun ComposeContentTestRule.waitForTag(tag: String, timeoutMillis: Long = 5_000) {
    waitUntil(timeoutMillis) {
        onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
    }
}

@RunWith(RobolectricTestRunner::class)
class SelfTest {
    @get:Rule
    val compose = createComposeRule()

    private lateinit var env: TestEnv

    @Before
    fun setUp() {
        env = testContainer(ApplicationProvider.getApplicationContext())
    }

    @After
    fun tearDown() {
        env.database.close()
    }

    @Test
    fun empty_page_offers_the_first_action() {
        compose.setContent { AppRoot(env.container) }
        compose.waitForText("Nothing here yet")
        compose.onNodeWithText("Add your first item").assertExists()
    }

    @Test
    fun adds_and_renames_an_item() {
        compose.setContent { AppRoot(env.container) }
        compose.waitForText("Nothing here yet")

        compose.onNodeWithContentDescription("Add item").performClick()
        compose.waitForTag("title-field")
        compose.onNodeWithTag("title-field").performTextInput("Buy milk")
        compose.onNodeWithContentDescription("Back").performClick()

        compose.waitForText("Buy milk")
        val saved = runBlocking { env.container.repository.observeAll().first() }
        assertEquals(listOf("Buy milk"), saved.map { it.title })
    }

    @Test
    fun stars_an_item_and_finds_it_on_the_starred_page() {
        runBlocking { env.database.seed() }
        compose.setContent { AppRoot(env.container) }
        val first = seedItems.first().title
        compose.waitForText(first)

        compose.onAllNodesWithContentDescription("Star")[0].performClick()
        compose.onNodeWithTag(Page.Starred.tag).performClick()

        compose.waitForText(first)
        compose.waitForText(seedItems.first { it.starred }.title)
    }

    @Test
    fun deletes_an_item_and_undo_brings_it_back() {
        runBlocking { env.database.seed() }
        compose.setContent { AppRoot(env.container) }
        val first = seedItems.first().title
        compose.waitForText(first)

        compose.onNodeWithText(first).performClick()
        compose.waitForTag("title-field")
        compose.onNodeWithContentDescription("Delete").performClick()

        compose.waitForText("Item deleted")
        compose.onNodeWithText(first).assertDoesNotExist()

        compose.onNodeWithText("Undo").performClick()
        // The list keeps its scroll position, so the restored row can sit above the screen.
        compose.waitUntil(5_000) { runBlocking { env.container.repository.observeAll().first() }.any { it.title == first } }
        compose.onNode(hasScrollAction()).performScrollToIndex(0)
        compose.waitForText(first)
    }

    @Test
    fun accent_choice_is_saved() {
        compose.setContent { AppRoot(env.container) }
        compose.onNodeWithTag(Page.Settings.tag).performClick()

        compose.onNodeWithContentDescription("Green accent").performClick()
        compose.waitUntil(5_000) {
            runBlocking { env.container.settings.state.first().accent } == Accent.Green
        }
        compose.onNodeWithContentDescription("Green accent").assertIsSelected()
    }
}

@RunWith(RobolectricTestRunner::class)
class StorageSelfTest {
    @Test
    fun data_survives_reopening_the_database() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val file = File(createTempDirectory("db").toFile(), "app.db")

        val first = testContainer(context, file)
        first.container.repository.add("Keep me")
        first.database.close()

        val second = testContainer(context, file)
        val titles = second.container.repository.observeAll().first().map { it.title }
        second.database.close()

        assertEquals(listOf("Keep me"), titles)
    }
}
