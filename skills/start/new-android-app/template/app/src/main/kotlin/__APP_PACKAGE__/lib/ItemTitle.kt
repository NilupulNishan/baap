package __APP_PACKAGE__.lib

/**
 * Pure logic with no Android classes, so it runs in a plain unit test.
 * Put rules and calculations here, and keep screens thin.
 */
object ItemTitle {
    private val whitespace = Regex("\\s+")

    /** Trims the title and collapses runs of spaces. Returns "" when nothing is left. */
    fun clean(raw: String): String = raw.trim().replace(whitespace, " ")
}
