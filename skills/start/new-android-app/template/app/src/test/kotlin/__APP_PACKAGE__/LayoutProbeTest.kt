package __APP_PACKAGE__

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import __APP_PACKAGE__.ui.AppRoot
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.abs
import kotlin.math.round

/**
 * Check 2 of the layout grid: measures the real boxes. Nodes marked with
 * `Modifier.gridProbe("area/name")` must start on the 4 dp grid, and nodes in
 * one area must share a leading edge.
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp-xhdpi")
class LayoutProbeTest {
    @get:Rule
    val compose = createComposeRule()

    private val env = testContainer(ApplicationProvider.getApplicationContext())

    @After
    fun tearDown() {
        env.database.close()
    }

    private data class Probe(val name: String, val left: Float, val top: Float)

    private fun ComposeContentTestRule.probes(): List<Probe> =
        onAllNodes(
            SemanticsMatcher("has a grid probe") {
                it.config.getOrNull(SemanticsProperties.TestTag)?.startsWith(PREFIX) == true
            },
        ).fetchSemanticsNodes().map { node ->
            val density = node.layoutInfo.density.density
            Probe(
                name = node.config[SemanticsProperties.TestTag].removePrefix(PREFIX),
                left = node.boundsInRoot.left / density,
                top = node.boundsInRoot.top / density,
            )
        }

    private fun check(label: String, probes: List<Probe>, expectedAreas: Set<String>) {
        val problems = mutableListOf<String>()
        val areas = probes.map { it.name.substringBefore('/') }.toSet()
        for (area in expectedAreas - areas) problems += "$label: no probes found for area '$area'"

        for (probe in probes) {
            if (!onGrid(probe.left)) problems += "$label: ${probe.name} starts at x=${probe.left} dp, off the grid"
            if (!onGrid(probe.top)) problems += "$label: ${probe.name} starts at y=${probe.top} dp, off the grid"
        }
        for ((area, members) in probes.groupBy { it.name.substringBefore('/') }) {
            val edges = members.map { it.left }
            if (edges.max() - edges.min() > TOLERANCE) {
                val detail = members.joinToString { "${it.name} ${it.left}" }
                problems += "$label: edge $area has ${edges.distinctBy { round(it) }.size} leading edges: $detail"
            }
        }

        if (problems.isEmpty()) println("PASS $label: ${probes.size} probes") else problems.forEach { println("FAIL $it") }
        assertTrue(problems.joinToString("\n"), problems.isEmpty())
    }

    @Test
    fun list_rows_share_one_edge_on_the_grid() {
        runBlocking { env.database.seed() }
        compose.setContent { AppRoot(env.container, startPage = Page.All) }
        compose.waitForText(seedItems.first().title)
        check("all-layout", compose.probes(), setOf("items"))
    }

    @Test
    fun settings_parts_share_one_edge_on_the_grid() {
        compose.setContent { AppRoot(env.container, startPage = Page.Settings) }
        compose.waitForIdle()
        check("settings-layout", compose.probes(), setOf("settings"))
    }

    @Test
    fun detail_title_is_on_the_grid() {
        compose.setContent { AppRoot(env.container) }
        compose.waitForText("Nothing here yet")
        compose.onNodeWithContentDescription("Add item").performClick()
        compose.waitForTag("grid-probe:detail/title")
        check("detail-layout", compose.probes(), setOf("detail"))
    }

    private fun onGrid(value: Float): Boolean = abs(value - round(value / 4f) * 4f) <= TOLERANCE

    private companion object {
        const val PREFIX = "grid-probe:"
        const val TOLERANCE = 0.5f
    }
}
