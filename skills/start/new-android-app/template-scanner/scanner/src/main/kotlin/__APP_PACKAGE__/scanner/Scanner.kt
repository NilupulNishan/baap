package __APP_PACKAGE__.scanner

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/** One barcode read. [symbology] is DataWedge's label type, like `LABEL-TYPE-EAN13`. */
data class Scan(val data: String, val symbology: String)

/**
 * A source of scans. [DataWedgeScanner] reads a rugged device's built-in scanner, and
 * [FakeScanner] drives tests and phones without one.
 */
interface Scanner {
    val scans: Flow<Scan>

    /** Turns the scanner on or off, for example while a dialog is open. */
    fun setEnabled(enabled: Boolean) {}

    /** Presses the trigger from code. Useful for an on-screen "Scan" button. */
    fun softTrigger() {}

    /** Runs [block] for every scan until [scope] ends. */
    fun onScan(scope: CoroutineScope, block: suspend (Scan) -> Unit): Job =
        scope.launch { scans.collect { block(it) } }
}
