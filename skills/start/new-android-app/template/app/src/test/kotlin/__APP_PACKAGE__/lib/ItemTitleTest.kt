package __APP_PACKAGE__.lib

import org.junit.Assert.assertEquals
import org.junit.Test

class ItemTitleTest {
    @Test
    fun trims_the_ends() {
        assertEquals("Buy milk", ItemTitle.clean("  Buy milk  "))
    }

    @Test
    fun collapses_runs_of_whitespace() {
        assertEquals("Buy oat milk", ItemTitle.clean("Buy   oat\t milk"))
    }

    @Test
    fun blank_input_becomes_empty() {
        assertEquals("", ItemTitle.clean("   "))
    }
}
