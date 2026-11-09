package __APP_PACKAGE__.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** Screens on the back stack. Tabs is the shell with the bottom bar (or rail). */
@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object Tabs : Route

    @Serializable
    data class Detail(val id: Long) : Route
}

/**
 * Marks a node for the layout probe test (`./gradlew grid`). Nodes that share an
 * area name (the part before the slash) must share a leading edge, and every
 * probe must sit on the 4 dp grid.
 */
fun Modifier.gridProbe(name: String): Modifier = testTag("grid-probe:$name")
