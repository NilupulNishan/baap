package __APP_PACKAGE__

import android.app.Application
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import __APP_PACKAGE__.scanner.DataWedgeScanner
import __APP_PACKAGE__.scanner.FakeScanner
import __APP_PACKAGE__.scanner.Scan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.LooperMode

/** The scanner path without a device: a fake DataWedge broadcast in, an item out. */
@RunWith(RobolectricTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
class ScanTest {
    private val app = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun a_datawedge_broadcast_becomes_a_scan() = runBlocking {
        val scanner = DataWedgeScanner(app)
        val received = async(start = CoroutineStart.UNDISPATCHED) { scanner.scans.first() }
        // The receiver registers when the flow starts, so give it a moment before the broadcast.
        delay(100)

        app.sendBroadcast(
            Intent(scanner.scanAction)
                .putExtra(DataWedgeScanner.EXTRA_DATA, "0123456789")
                .putExtra(DataWedgeScanner.EXTRA_LABEL_TYPE, "LABEL-TYPE-EAN13"),
        )
        shadowOf(android.os.Looper.getMainLooper()).idle()

        assertEquals(Scan("0123456789", "LABEL-TYPE-EAN13"), withTimeout(5_000) { received.await() })
    }

    @Test
    fun the_scanner_asks_datawedge_for_a_profile() = runBlocking {
        val scanner = DataWedgeScanner(app)
        val job = async(start = CoroutineStart.UNDISPATCHED) { scanner.scans.first() }
        delay(100)
        shadowOf(android.os.Looper.getMainLooper()).idle()

        val setConfig = shadowOf(app).broadcastIntents
            .firstOrNull { it.action == DataWedgeScanner.ACTION_API }
        assertNotNull("no SET_CONFIG broadcast was sent", setConfig)
        assertEquals(DataWedgeScanner.DATAWEDGE_PACKAGE, setConfig!!.`package`)
        val profile = setConfig.getBundleExtra(DataWedgeScanner.API_SET_CONFIG)
        assertEquals(app.packageName, profile?.getString("PROFILE_NAME"))
        job.cancel()
    }

    @Test
    fun a_scan_adds_an_item() = runBlocking {
        val env = testContainer(app)
        val scope = CoroutineScope(Dispatchers.Unconfined)
        val scanner = FakeScanner()
        scanner.onScan(scope) { env.container.repository.add(it.data) }
        scanner.scan("0123456789")

        val items = withTimeout(5_000) { env.container.repository.observeAll().first { it.isNotEmpty() } }
        assertEquals(listOf("0123456789"), items.map { it.title })
        scope.cancel()
        env.database.close()
    }
}
