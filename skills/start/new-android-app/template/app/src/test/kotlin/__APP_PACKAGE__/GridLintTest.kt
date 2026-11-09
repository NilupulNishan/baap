package __APP_PACKAGE__

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Check 1 of the layout grid: every dp number in the source is a multiple of 4.
 * Allowed: 1.dp and 2.dp for hairlines and strokes. Add `// grid-ok` to a line
 * that needs another number, with the reason after it.
 */
class GridLintTest {
    private val dp = Regex("""(\d+(?:\.\d+)?)\.dp\b""")

    @Test
    fun every_dp_value_is_on_the_4dp_grid() {
        val files = File("src/main/kotlin").walkTopDown().filter { it.extension == "kt" }.toList()
        assertTrue("no source files found; run from the app module", files.isNotEmpty())

        val offenders = mutableListOf<String>()
        for (file in files) {
            file.readLines().forEachIndexed { index, line ->
                if ("grid-ok" in line) return@forEachIndexed
                for (match in dp.findAll(line)) {
                    val value = match.groupValues[1].toDouble()
                    if (value % 4.0 != 0.0 && value != 1.0 && value != 2.0) {
                        offenders += "${file.path}:${index + 1}: off grid (${match.value}): ${line.trim()}"
                    }
                }
            }
        }

        if (offenders.isEmpty()) {
            println("PASS grid-lint: ${files.size} files")
        } else {
            offenders.forEach { println("FAIL $it") }
        }
        assertTrue("${offenders.size} lines off the 4 dp grid:\n" + offenders.joinToString("\n"), offenders.isEmpty())
    }
}
