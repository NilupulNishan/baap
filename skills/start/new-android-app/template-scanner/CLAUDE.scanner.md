
## Hardware scanner

This app was made with `--scanner`. The `:scanner` module reads barcodes through
DataWedge, the scanning service preinstalled on some rugged Android devices. It
uses intents only: no SDK, no licence.

- `scanner/` has `Scanner` (the interface), `DataWedgeScanner` (real), and
  `FakeScanner` (tests and emulators). `App.kt` starts the scanner, and every scan adds an item.
- On first run `DataWedgeScanner` creates a DataWedge profile named after the package. Change the
  profile in code, or delete it in the DataWedge app, to see a change.
- Simulate a scan on any device or emulator (the receiver is exported):

  ```bash
  adb shell am broadcast -a <package>.SCAN \
    --es com.symbol.datawedge.data_string 0123456789 \
    --es com.symbol.datawedge.label_type LABEL-TYPE-EAN13
  ```

- Only a device with DataWedge proves the real path: press the trigger and watch the item appear.
- The plugin's `rugged-scanning` skill covers DataWedge profiles, EMDK, and keystroke fallback.
  `rugged-device-config` covers staging devices, and `label-printing` and `rfid-reading` cover the rest.
