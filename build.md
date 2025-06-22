# Establishing a CLI based Development Environment
## On Linux

From [java - How to make an Android app without using Android Studio? - Stack Overflow](https://stackoverflow.com/questions/32643297/how-to-make-an-android-app-without-using-android-studio)

1. Set the environment variables as described in the [documentation](https://developer.android.com/tools/variables).

* Download the [`cmdline-tools`](https://developer.android.com/studio#command-line-tools-only), which include core tools as [`sdkmanager`](https://developer.android.com/studio/command-line/sdkmanager) to manage the SDK components, [`avdmanager`](https://developer.android.com/studio/command-line/avdmanager) for emulators, as well as debugging and tuning tools like [`r8`](https://r8.googlesource.com/r8) and [`lint`](https://developer.android.com/studio/write/lint#commandline).

* Use `sdkmanager` to install the following packages:

  * [`build-tools`](https://developer.android.com/tools#tools-build) -- Contains basic toolchain programs like [`aapt`](https://developer.android.com/studio/command-line/aapt2), [`apksigner`](https://developer.android.com/studio/command-line/apksigner), and [`d8`](https://developer.android.com/studio/command-line/d8).
  * `platforms` -- Contains the Android platform, i.e. API level, you want to develop for. Note, that you can use several platforms in parallel at need.
  * [`platform-tools`](https://developer.android.com/tools#tools-platform) -- Contains instruments, e.g. the important [`adb`](https://developer.android.com/studio/command-line/adb), that interface with the corresponding Android platform. Choose the `platform-tools` according to the before selected `platforms` packages.
  * [`ndk`](https://developer.android.com/ndk/downloads/) -- Needed for performant [native programming](https://developer.android.com/ndk/guides/).

* Use [`avdmanager`](https://developer.android.com/studio/command-line/avdmanager) to manage the Android device emulators of the Android SDK.

* If required, also install [`gradle`](https://gradle.org/) according to the [Installation](https://gradle.org/install) guide. Gradle can then be used from CLI, as described in its [Command-Line Interface](https://docs.gradle.org/current/userguide/command_line_interface.html) user guide.
```
gradle wrapper --gradle-version 8.9
./gradlew clean
./gradlew build
```

* Connect Android phone as described in [How to use wireless ADB on your Android phone or tablet](https://www.androidpolice.com/use-wireless-adb-android-phone/)
```
adb pair
adb connect ip:port
adb devices
adb -s device install *.apk
```

From [sdkmanager](https://developer.android.com/tools/sdkmanager)

1. Download the latest [command line tools package](/studio#command-line-tools-only) from the [Android Studio](/studio) page and extract the package.
2. Move the unzipped *cmdline-tools* directory into a new directory
of your choice, such as android_sdk. This new directory is your
Android SDK directory.
3. In the unzipped *cmdline-tools* directory, create a
sub-directory called *latest*.
4. Move the original *cmdline-tools* directory contents, including
the *lib* directory, *bin* directory,
*NOTICE.txt* file, and *source.properties* file, into the
newly created *latest* directory. You can now use the command-line
tools from this location.
