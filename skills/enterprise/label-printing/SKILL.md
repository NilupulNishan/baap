---
name: label-printing
description: Print labels from an Android app to label printers with the Link-OS SDK over Bluetooth or the network, or with raw ZPL over TCP, including a printer status check, work off the main thread, and testing with no printer. Use when the app prints labels or receipts to a label printer, when the user mentions ZPL, Link-OS, or a mobile printer such as the ZQ series.
---

# Label printing

Label printers that use ZPL take label data as ZPL text (or CPCL on some mobile models). The app builds the text and sends it
over a connection. Two routes:

| | Link-OS SDK | Raw ZPL over TCP |
| --- | --- | --- |
| What it is | The printer maker's Java SDK for Android: connections, status, settings | A plain socket to the printer's network port |
| Transports | Bluetooth and TCP | Network printers only |
| Printer status before printing | Yes | No |
| Extra setup | A jar in `app/libs` and Bluetooth permissions | None |
| Choose it when | Mobile printers over Bluetooth, or you need status | A fixed network printer and a simple label |

Link-OS is a trademark of its owner. The SDK comes from the printer maker: its Link-OS page says the latest
release is available at `www.zebra.com/linkossdk` with libraries, API documentation, and developer demos.

## 1. Put a seam in front of the printer

Screens never call the SDK. They talk to an interface, and tests use a fake:

```kotlin
interface LabelPrinter {
    suspend fun print(zpl: String): PrintResult
}

sealed interface PrintResult {
    data object Sent : PrintResult
    data class Failed(val reason: String) : PrintResult   // "Printer is out of paper", "Unable to connect"
}

class FakeLabelPrinter : LabelPrinter {
    val sent = mutableListOf<String>()
    override suspend fun print(zpl: String): PrintResult { sent += zpl; return PrintResult.Sent }
}
```

Keep the label text in `lib/` (a function from your data to a ZPL string) with a unit test, so the label is
tested with no printer.

## 2. Build the ZPL

A label starts with `^XA` and ends with `^XZ`. `^FO` sets the position, `^A0N` the font, `^FD` the field data,
`^FS` ends a field, `^BC` draws a Code 128 barcode. The Link-OS sample prints this test label:

```text
^XA^FO17,16^GB379,371,8^FS^FT65,255^A0N,135,134^FDTEST^FS^XZ
```

An item label with a barcode:

```text
^XA^FO50,40^A0N,40,40^FDItem 42^FS^FO50,100^BCN,100,Y,N,N^FD0123456789^FS^XZ
```

Check each command and its parameters in the ZPL programming guide before you rely on it. Escape or strip
`^` and `~` from user text, since they start commands.

## 3. Link-OS SDK route

1. Download the Link-OS SDK for Android and copy the jar files from the download into `app/libs/`. The sample app
   keeps `ZSDK_ANDROID_API.jar` and its dependency jars there. Use whatever the current download contains, and in
   `app/build.gradle.kts` add `implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))`.
2. Permissions. Android 12 and later: declare `BLUETOOTH_CONNECT` (and `BLUETOOTH_SCAN` if you search for
   printers) and request them at runtime, after a tap. Android 11 and earlier: `BLUETOOTH` and `BLUETOOTH_ADMIN`
   with `android:maxSdkVersion="30"`. Network printers need `INTERNET`. The sample declares `BLUETOOTH`,
   `BLUETOOTH_ADMIN`, `CHANGE_WIFI_MULTICAST_STATE`, and `INTERNET`, and predates the Android 12 permissions.
3. Connect, check status, write, close, always off the main thread. These calls, from the Link-OS sample, are blocking:

   ```kotlin
   class LinkOsLabelPrinter(private val newConnection: () -> Connection) : LabelPrinter {
       override suspend fun print(zpl: String): PrintResult = withContext(Dispatchers.IO) {
           val connection = newConnection()          // BluetoothConnection(macAddress) or TcpConnection(host, port)
           try {
               connection.open()
               val printer = ZebraPrinterFactory.getInstance(connection)
               val linkOs = ZebraPrinterFactory.createLinkOsPrinter(printer)
               val status = linkOs?.currentStatus ?: printer.currentStatus
               when {
                   status.isHeadOpen -> PrintResult.Failed("The printer head is open.")
                   status.isPaperOut -> PrintResult.Failed("The printer is out of paper.")
                   status.isPaused -> PrintResult.Failed("The printer is paused.")
                   !status.isReadyToPrint -> PrintResult.Failed("The printer is not ready.")
                   else -> { connection.write(zpl.toByteArray()); PrintResult.Sent }
               }
           } catch (e: ConnectionException) {
               PrintResult.Failed("Unable to reach the printer.")
           } finally {
               try { connection.close() } catch (_: ConnectionException) {}
           }
       }
   }
   ```

   The classes are `com.zebra.sdk.comm.BluetoothConnection`, `TcpConnection`, `Connection`, `ConnectionException`, and
   `com.zebra.sdk.printer.ZebraPrinterFactory`, `ZebraPrinter`, `ZebraPrinterLinkOs`, `PrinterStatus`. `getInstance` can also
   throw `ZebraPrinterLanguageUnknownException`, so catch it the same way.
4. Finding printers: the simplest reliable way is to let the user enter or scan the printer's address once and keep
   it in DataStore. The SDK also has discovery classes for Bluetooth and the network: read its API documentation
   from the download before you use them.
5. A printer language other than ZPL: the sample reads `SGD.GET("device.languages", connection)` and
   `printer.getPrinterControlLanguage()` (`PrinterLanguage.ZPL` or `CPCL`), so check before you send ZPL to an older mobile printer.

## 4. Raw ZPL over TCP

Network printers are conventionally reached on TCP port 9100 for raw print data (confirm the port in the printer's
user guide, and that raw printing is enabled). With plain Java sockets, off the main thread:

```kotlin
suspend fun sendRaw(host: String, zpl: String, port: Int = 9100): PrintResult = withContext(Dispatchers.IO) {
    try {
        Socket().use { socket ->
            socket.connect(InetSocketAddress(host, port), 3_000)
            socket.getOutputStream().apply { write(zpl.toByteArray()); flush() }
        }
        PrintResult.Sent
    } catch (e: IOException) {
        PrintResult.Failed("Unable to reach the printer at $host.")
    }
}
```

Add a timeout, a clear error, and a retry button. There is no status check on this route.

## 5. Test without a printer

- Unit-test the ZPL builder, and test screens with `FakeLabelPrinter`.
- Listen on your own computer and point the app at it, on the same network:

  ```bash
  node -e "require('net').createServer(s => s.pipe(process.stdout)).listen(9100)"
  ```

  The label text prints in the terminal. An emulator reaches the computer at `10.0.2.2`.
- To see the label, paste the ZPL into an online ZPL viewer. Send label data to an online service only when it is not
  sensitive.
- Do a final test on a real printer with real labels: only the printer shows whether the label fits.

Done when screens print through `LabelPrinter` only, the ZPL builder has a unit test, no SDK call runs on the main thread, the
status is checked before printing on the Link-OS route (or the lack of it is noted in the brief), a failed print shows a
message that says what to do, and one label was printed on a real printer or its absence is named as a manual check.

## Sources

- https://techdocs.zebra.com/link-os/
- https://techdocs.zebra.com/link-os/2-14/android
- https://github.com/ZebraDevs/LinkOS-Android-Samples (branch `ConnectivityDemo`: `MainActivity.java`, `app/build.gradle`, `AndroidManifest.xml`)
- https://developer.android.com/guide/topics/connectivity/bluetooth/permissions
- Port 9100: widely documented in third-party guides, not confirmed on the printer maker's pages during research. Confirm it in the printer's user guide.
