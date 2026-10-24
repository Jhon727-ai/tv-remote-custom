# TV Remote Custom

Aplikasi remote custom untuk Android TV yang bisa diakses via web UI lokal. Tidak perlu root, hanya butuh enable Accessibility Service sekali.

## Cara Install

1. Download APK dari releases
2. Install via ADB:
   ```bash
   adb install tv-remote-custom-release.apk
   ```
3. Buka app → klik **ENABLE ACCESSIBILITY**
4. Kembali ke app → klik **START SERVER**
5. Buka browser: `http://<IP_TV>:8080`

## Fitur

- Virtual D-Pad + tombol tengah OK
- Back, Home, Menu
- Volume +/-, Mute
- Power shortcut
- Launch app (TV, YouTube, Netflix)
- Text input untuk search
- 100% lokal, tidak perlu internet

## Build

```bash
./gradlew assembleRelease
```

APK akan ada di: `app/build/outputs/apk/release/`

## Requirements

- Android 8.0+ (API 26)
- Accessibility Service enabled
