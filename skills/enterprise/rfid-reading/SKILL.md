---
name: rfid-reading
description: Read RFID tags in an Android app on handheld and sled RFID readers, choosing between DataWedge RFID input (the simpler route) and the reader maker's RFID SDK for Android, with a seam for tests and a plan for sleds versus built-in readers. Use when the app must read, inventory, or locate RFID tags, or the user mentions an RFD40, RFD90, RFD8500, or a handheld with a built-in RFID reader.
---

# RFID reading

RFID reads many tags at once from a distance: an inventory pass over a shelf, or a search for one tag. Readers that
work with DataWedge reach an Android app in two ways. The reader names are trademarks of their owners.

| | DataWedge RFID input | RFID SDK for Android |
| --- | --- | --- |
| What it is | RFID as another DataWedge input source, delivered like barcode data | An Android library (an `.aar`) with APIs for readers, inventory, and tag events |
| In the app | The intents you already use for scanning | A library, a download, and its own lifecycle |
| Control | A profile decides what is read and how | The app starts and stops inventory and sets antenna power |
| Choose it when | One read per trigger pull is enough, and IT can own the settings | The app needs live tag events, tag locating, or per-read control |

Start with DataWedge when it is enough: it reuses the `Scanner` seam from `rugged-scanning`. Move to the SDK when the
stories need what DataWedge profiles cannot give.

## 1. Know the hardware

Ask which readers the fleet uses, and write it in `docs/BRIEF.md`:

- **Sleds** attach to a phone or handheld computer. The SDK pages name the RFD8500 (a Bluetooth sled), the RFD2000
  (a handheld sled for the TC20), and the RFD40 and RFD90. A sled over Bluetooth needs a paired reader and the Bluetooth
  permissions (`BLUETOOTH_CONNECT` and `BLUETOOTH_SCAN` at runtime on Android 12 and later).
- **Built-in readers** are part of a handheld computer, such as the MC3300 series with RFID (the SDK download page names
  the MC3300XR). The app talks to the reader on the same device.
- **Fixed readers** (the FX9600 and FX7500 are named) are mounted on a wall or a gate and are not driven from a handheld app.

The SDK's download page lists the minimum Android version by reader: Android 8.0 and later for the RFD8500, and
Android 10 and later for the MC3300XR, RFD40, and RFD90. Developers need Android Studio and API level 26 or later.

## 2. Put a seam in front of the reader

Whichever route you use, screens talk to an interface, and tests use a fake, as with scanning:

```kotlin
data class TagRead(val tagId: String, val rssi: Int? = null)

interface TagReader {
    val reads: Flow<TagRead>
    suspend fun startInventory()
    suspend fun stopInventory()
}

class FakeTagReader : TagReader {
    private val flow = MutableSharedFlow<TagRead>(extraBufferCapacity = 64)
    override val reads: Flow<TagRead> = flow
    var running = false
        private set
    override suspend fun startInventory() { running = true }
    override suspend fun stopInventory() { running = false }
    fun read(tagId: String) { flow.tryEmit(TagRead(tagId)) }
}
```

De-duplicate in `lib/`: an inventory pass reports the same tag many times, so keep a set of seen tag ids and count
once. Test that with the fake.

## 3. DataWedge route

DataWedge lists `RFID` among its input plugins, and its `SET_CONFIG` command accepts `PLUGIN_NAME` `RFID`. The DataWedge docs describe RFID as another input source,
with data delivery similar to barcode and magnetic-stripe input. The scan intent carries
`com.symbol.datawedge.source`, which is `rfid` for RFID data.

1. Configure the RFID input in a profile on one device in the DataWedge app, and test that the tag data arrives in the same
   intent output as barcodes (`rugged-scanning`).
2. Export the profile (`rugged-device-config`) so IT can deploy it. The parameter names of the RFID plugin were not verified for this
   skill: read them from an exported profile or from the DataWedge RFID input page before you write `SET_CONFIG` code.
3. Read `com.symbol.datawedge.source` in the receiver so barcode and RFID reads do not mix (the template's `DataWedgeScanner`
   ignores it; add a field to `Scan` if the app needs both).
4. Check what `com.symbol.datawedge.data_string` holds for a real tag before you parse it.

## 4. RFID SDK route

1. Download the RFID SDK for Android from the reader maker's developer portal (the download page shows terms to accept, and
   Login and Register links; the maker requires email multi-factor authentication on its sign-in sites from July 1, 2026).
   Put the `.aar` in `app/libs/` and add it with `implementation(files("libs/<file>.aar"))`, using the real file name.
2. Read the SDK's developer guide and the "Hello RFID" sample for the current API: reader discovery, connect and disconnect,
   starting and stopping inventory, the tag read event, and locating a tag. Wrap those calls inside a class that
   implements `TagReader`, and name the sample's class and method names in the code comments so a reader can find them.
3. Run all reader calls and listeners off the main thread, post results to a `Flow`, and release the reader in `onDestroy`.
4. Handle the real failures: the sled is off, out of range, or unpaired; the battery is low; Bluetooth is off. Each one
   gets a message that says what to do.

## 5. Test

- JVM: `FakeTagReader` drives the inventory screen and the de-duplication. No device needed.
- Device: only a reader proves antenna power, range, and tag formats. Write each as a named manual check in the story,
  with the tags and reader model used.

Done when screens read tags through `TagReader` only, de-duplication has a unit test, reader calls run off the main
thread, a missing or disconnected reader shows a message that says what to do, and a real read on the target
reader is done or named as a manual check.

## Sources

- https://developer.zebra.com/products/rfid/android-rfid-reader-sdk
- https://www.zebra.com/us/en/support-downloads/software/developer-tools/rfid-sdk-for-android.html
- https://developer.zebra.com/community/technologies/rfid
- https://developer.zebra.com/products/enterprise-mobile-computing/datawedge
- https://techdocs.zebra.com/datawedge/latest/guide/api/setconfig/
- https://techdocs.zebra.com/datawedge/latest/guide/output/intent/
- https://techdocs.zebra.com/datawedge/latest/guide/profiles/
- https://www.zebra.com/mfa-sso
- Not verified during research (TechDocs RFID pages did not load): the SDK's class and method names, its manifest
  permissions, and the RFID plugin parameters. This skill sends the agent to the SDK's guide and sample for them.
