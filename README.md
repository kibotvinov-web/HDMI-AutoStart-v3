# HDMI AutoStart — Ночные Волки Пятнашка

Android 11 / Salute TV / MediaTek. Показывает заставку 6 секунд, затем запускает HDMI 3 (HW6).

Загрузите содержимое архива **в корень репозитория GitHub**, запустите Actions → Build Android APK. Скачайте Artifacts → HDMI-AutoStart-APK.

Установка: `adb install -r app-debug.apk`. Проверка: `adb shell am start -n ru.hartens.hdmiautostart/.MainActivity`.

Чтобы назначить HOME: `adb shell cmd package set-home-activity ru.hartens.hdmiautostart/.MainActivity`. Возврат: `adb shell cmd package set-home-activity ru.sberdevices.starlauncher/.ui.mainscreen.MainActivity`.
