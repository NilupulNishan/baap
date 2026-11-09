# Set up the Android toolchain

BAAP apps need three things: a JDK (17 or later), the Android SDK, and Node
20 or later for the scaffold script. All checks run on the JVM, so you do
not need an emulator or a phone to build, test, and snapshot.

Check your machine at any time:

```bash
node "${CLAUDE_SKILL_DIR}/scripts/doctor.mjs"
```

It prints PASS, or one fix per problem.

## Fastest way: Android Studio

Install [Android Studio](https://developer.android.com/studio). It brings a
JDK and the SDK. Open it once, let it finish the first-run setup, then run
the doctor. Set `ANDROID_HOME` to the SDK folder shown in Settings, Languages
and Frameworks, Android SDK.

## Without Android Studio

### 1. A JDK

| OS | Command |
| --- | --- |
| Windows | `winget install --id Microsoft.OpenJDK.21 -e` (asks for admin), or unzip the build from https://aka.ms/download-jdk/microsoft-jdk-21-windows-x64.zip into `%USERPROFILE%\.jdks` and set `JAVA_HOME` |
| macOS | `brew install openjdk@21`, then `export JAVA_HOME=$(/usr/libexec/java_home -v 21)` |
| Linux | `sudo apt install openjdk-21-jdk` (or your package manager's JDK 17 or 21) |

`java -version` prints 17 or later.

### 2. The Android SDK

1. Download "Command line tools only" from
   https://developer.android.com/studio#command-line-tools-only and unzip it.
2. Move the unzipped `cmdline-tools` folder so this file exists:
   - Windows: `%LOCALAPPDATA%\Android\Sdk\cmdline-tools\latest\bin\sdkmanager.bat`
   - macOS: `~/Library/Android/sdk/cmdline-tools/latest/bin/sdkmanager`
   - Linux: `~/Android/Sdk/cmdline-tools/latest/bin/sdkmanager`
3. Set `ANDROID_HOME` to the `Sdk` folder, and add `platform-tools` to `PATH`.
4. Install the packages and accept the licenses:

   ```bash
   sdkmanager "platform-tools" "platforms;android-37.0" "build-tools;37.0.0"
   sdkmanager --licenses
   ```

Gradle downloads any other package it needs the first time you build, as
long as the licenses are accepted.

### 3. Check

```bash
node "${CLAUDE_SKILL_DIR}/scripts/doctor.mjs"
```

Done when the doctor prints PASS.
