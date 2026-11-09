---
name: release
description: Release an Android app as a signed bundle for Google Play, a signed APK for a GitHub Release or sideloading, or a build for company-managed devices, and check the release build. Use when the user wants to release, publish, ship, sign, upload to Google Play, share an APK, or asks how to get the app onto phones.
argument-hint: "[target: play | apk | managed]"
---

# Release

A release is a signed build that people can install. Debug builds are not releases: they are slower,
debuggable, and signed with a throwaway key.

Releasing publishes the app. Confirm the target (Play, a GitHub Release, or company devices) and that
the code and the app may be public before the first release.

## 1. Check first

```bash
./gradlew check
```

It exits 0. Never release a build that fails its checks. Then read `app/build.gradle.kts`:

- `applicationId` is the real one. Google Play rejects `com.example.*`, and the id cannot change after
  the first release.
- `versionCode` is higher than every build you uploaded before (a whole number). `versionName` is the
  version people see (`1.0.0`).
- `isMinifyEnabled = true` and `isShrinkResources = true` in `release` (the template has both).
- `targetSdk` meets Google Play's current target API requirement (see "Target API level requirements"
  in Play Console help). The template's `targetSdk` is 36.
- The launcher icon and the name are the app's own (`app-icon` skill).

## 2. Sign

Release builds need a key. Keep it out of git and out of chat.

1. The user creates the keystore themselves, because it asks for passwords. Suggest they run it in this
   session with the `!` prefix:

   ```bash
   ! keytool -genkeypair -v -keystore release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
   ```

2. Create `keystore.properties` next to `settings.gradle.kts`, with the user's values. It is already in `.gitignore`, like `*.jks`:

   ```properties
   storeFile=release.jks
   storePassword=...
   keyAlias=upload
   keyPassword=...
   ```

3. Read it in `app/build.gradle.kts`, and sign only when it exists, so `check` still runs on a clean clone:

   ```kotlin
   import java.util.Properties

   val keystoreProperties = Properties().apply {
       val file = rootProject.file("keystore.properties")
       if (file.exists()) file.inputStream().use { load(it) }
   }

   android {
       signingConfigs {
           create("release") {
               if (keystoreProperties.isNotEmpty()) {
                   storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                   storePassword = keystoreProperties.getProperty("storePassword")
                   keyAlias = keystoreProperties.getProperty("keyAlias")
                   keyPassword = keystoreProperties.getProperty("keyPassword")
               }
           }
       }
       buildTypes {
           release {
               if (keystoreProperties.isNotEmpty()) signingConfig = signingConfigs.getByName("release")
           }
       }
   }
   ```

Back up `release.jks` and its passwords somewhere safe. With Play App Signing (step 3) the key you
hold is the upload key, which Google can reset. For a sideloaded APK it is the only key: lose it and
people must uninstall to update.

## 3. Google Play

```bash
./gradlew bundleRelease     # app/build/outputs/bundle/release/app-release.aab
```

1. Create the app in Play Console. Use Play App Signing, so Google holds the app signing key and you
   upload with your upload key.
2. Upload the `.aab` to **Internal testing** first, and add testers. Install from the test link on a real phone.
3. Fill in the store listing: name, short and full description, screenshots (the PNGs in `shots/`
   show the screens, on a phone frame if you like), the 512 px icon (`shots/icon-512.png`), and a
   feature graphic.
4. Complete App content: the **privacy policy URL**, the **Data safety** form, content rating, and
   target audience. A local-first app with no accounts and no network says it collects nothing. Say
   what the app really does: if it sends data anywhere, list it.
5. New personal developer accounts must run a closed test with a minimum number of testers before
   production access. Check the current requirement in Play Console help before you promise a date.
6. Promote the release from testing to production when the testers are happy.

Uploading to Play is outward-facing. Let the user press the final publish button.

## 4. APK for a GitHub Release or sideloading

```bash
./gradlew assembleRelease   # app/build/outputs/apk/release/app-release.apk
```

Without a signing config the APK is unsigned and will not install. Attach it to a release:

```bash
gh release create v1.0.0 app/build/outputs/apk/release/app-release.apk --title "1.0.0" --notes "What changed"
```

Tell people to allow "install unknown apps" for their browser or file manager once. Check with the user
before you create the release: it is public.

## 5. Company devices

Apps for managed devices go through Managed Google Play (a private app published to the company) or an
MDM that pushes the APK. Call the Skill tool with `managed-devices`. For rugged handhelds, also check
`rugged-device-config`.

## 6. Check the release build

The release build runs R8, so it can crash where debug did not (a missing keep rule). Install it and use it:

```bash
adb install -r app/build/outputs/apk/release/app-release.apk
adb shell am start -n <package>/.MainActivity
adb logcat -d | tail -50      # no "FATAL EXCEPTION"
```

Add an item, rename it, star it, delete it with Undo, restart the app, and confirm the data is there. Look
at Settings in light and dark. For an `.aab`, test the same build from the Play internal testing link.

## 7. Tell the user

Give the version, where it went (Play track, release URL, or MDM), how a new release happens (bump
`versionCode`, run the command), and where the keystore is backed up.

Done when the release build is signed, installed on a device or emulator and used once without a
crash, the version code is higher than the last one, no key or password is in git, and the user has the
link or file and the steps to release again.
