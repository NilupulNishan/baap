package __APP_PACKAGE__.scanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Reads barcodes through DataWedge, the scanning service preinstalled on some rugged
 * Android devices. It needs no SDK and no licence. On any other phone the commands go nowhere and
 * [scans] never emits.
 *
 * On first collect it creates a DataWedge profile named after the app's package,
 * with barcode input on and intent output as a broadcast to `<package>.SCAN`.
 * An existing profile is left as it is, so IT can tune it.
 * API reference: https://techdocs.zebra.com/datawedge/latest/guide/api/
 */
class DataWedgeScanner(context: Context) : Scanner {
    private val context = context.applicationContext
    private val profileName = this.context.packageName

    /** The action DataWedge broadcasts each scan to. */
    val scanAction: String = "${this.context.packageName}.SCAN"

    override val scans: Flow<Scan> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val data = intent.getStringExtra(EXTRA_DATA) ?: return
                trySend(Scan(data, intent.getStringExtra(EXTRA_LABEL_TYPE) ?: "LABEL-TYPE-UNDEFINED"))
            }
        }
        val filter = IntentFilter(scanAction).apply { addCategory(Intent.CATEGORY_DEFAULT) }
        // DataWedge is another app, so the receiver must be exported. Android 13 added the export flags,
        // and an app that targets Android 14 or later must pass one (ContextCompat applies it on every version).
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_EXPORTED)
        createProfile()
        awaitClose { context.unregisterReceiver(receiver) }
    }

    override fun setEnabled(enabled: Boolean) {
        send(API_SCANNER_INPUT_PLUGIN, if (enabled) "ENABLE_PLUGIN" else "DISABLE_PLUGIN")
    }

    override fun softTrigger() {
        send(API_SOFT_SCAN_TRIGGER, "TOGGLE_SCANNING")
    }

    private fun createProfile() {
        val barcode = Bundle().apply {
            putString("PLUGIN_NAME", "BARCODE")
            putString("RESET_CONFIG", "true")
            putBundle(
                "PARAM_LIST",
                Bundle().apply {
                    putString("scanner_selection", "auto")
                    putString("scanner_input_enabled", "true")
                },
            )
        }
        val intentOutput = Bundle().apply {
            putString("PLUGIN_NAME", "INTENT")
            putString("RESET_CONFIG", "true")
            putBundle(
                "PARAM_LIST",
                Bundle().apply {
                    putString("intent_output_enabled", "true")
                    putString("intent_action", scanAction)
                    putString("intent_category", Intent.CATEGORY_DEFAULT)
                    putInt("intent_delivery", DELIVERY_BROADCAST)
                },
            )
        }
        // Keystroke output off, so a scan does not also type into the focused field.
        val keystroke = Bundle().apply {
            putString("PLUGIN_NAME", "KEYSTROKE")
            putString("RESET_CONFIG", "true")
            putBundle("PARAM_LIST", Bundle().apply { putString("keystroke_output_enabled", "false") })
        }
        val app = Bundle().apply {
            putString("PACKAGE_NAME", context.packageName)
            putStringArray("ACTIVITY_LIST", arrayOf("*"))
        }
        val profile = Bundle().apply {
            putString("PROFILE_NAME", profileName)
            putString("PROFILE_ENABLED", "true")
            putString("CONFIG_MODE", "CREATE_IF_NOT_EXIST")
            putParcelableArrayList("PLUGIN_CONFIG", arrayListOf(barcode, intentOutput, keystroke))
            putParcelableArray("APP_LIST", arrayOf(app))
        }
        send(API_SET_CONFIG, profile)
    }

    private fun send(command: String, value: Bundle) = send { putExtra(command, value) }

    private fun send(command: String, value: String) = send { putExtra(command, value) }

    private fun send(extras: Intent.() -> Unit) {
        val intent = Intent(ACTION_API).setPackage(DATAWEDGE_PACKAGE).apply(extras)
        context.sendBroadcast(intent)
    }

    companion object {
        const val DATAWEDGE_PACKAGE = "com.symbol.datawedge"
        const val ACTION_API = "com.symbol.datawedge.api.ACTION"
        const val API_SET_CONFIG = "com.symbol.datawedge.api.SET_CONFIG"
        const val API_SOFT_SCAN_TRIGGER = "com.symbol.datawedge.api.SOFT_SCAN_TRIGGER"
        const val API_SCANNER_INPUT_PLUGIN = "com.symbol.datawedge.api.SCANNER_INPUT_PLUGIN"
        const val EXTRA_DATA = "com.symbol.datawedge.data_string"
        const val EXTRA_LABEL_TYPE = "com.symbol.datawedge.label_type"
        private const val DELIVERY_BROADCAST = 2
    }
}
