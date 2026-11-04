# HDMI AutoStart for Hartens / Salute TV (Android 11)

Experimental Android HOME launcher for opening MediaTek HDMI 3 (`HW6`) when the TV's Android home screen starts. It does not control TV AC power or turn the TV on from standby.

## Build
GitHub Actions > Build Android APK. Download artifact `HDMI-AutoStart-APK`; it contains `app-debug.apk`.

## Installation / test on PC
```cmd
adb connect 192.168.1.71:5555
adb install -r app-debug.apk
adb shell am start -n ru.hartens.hdmiautostart/.MainActivity
```
Check that HDMI 3 opens. If it doesn't, do NOT set this as HOME.

## Configure HOME only after the test passes
```cmd
adb shell cmd package set-home-activity ru.hartens.hdmiautostart/.MainActivity
adb shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME
```
Then press the Home key and finally test a power-off / power-on cycle.

## Restore Salute TV
```cmd
adb shell cmd package set-home-activity ru.sberdevices.starlauncher/.ui.mainscreen.MainActivity
adb shell am start -n ru.sberdevices.starlauncher/.ui.mainscreen.MainActivity
```
You can also launch Salute TV from the app itself. Do not uninstall or disable the system launcher.

## Caveats
- ADB `am start` success as the shell user does not guarantee Android permits the *app* to open the MediaTek input; verify before setting HOME.
- The firmware might ignore third-party HOME apps or return to Salute TV after restart.
- The TV still requires its power-button press after electricity returns.
- On Android 11, HOME foreground activity is more reliable than a bare background BOOT_COMPLETED receiver.
