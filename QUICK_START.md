# 🚀 Quick Start — Run androidApp in Android Studio

## TL;DR — 5 minutes to running app

### 1️⃣ Open in Android Studio
```
File → Open → /Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps
```

### 2️⃣ Wait for Gradle Sync
Top banner may show *Gradle sync*. Wait for it to finish (1-2 min).

### 3️⃣ Connect Device or Start Emulator
```bash
# Physical device: USB + Developer Mode + USB Debugging enabled

# OR start emulator:
Tools → Device Manager → Create Device (if needed) → Play
```

### 4️⃣ Click Run ▶️
- **Keyboard:** Ctrl+R (or ⌘R on Mac)
- **Button:** Green ▶️ in top toolbar

App builds (~2-3 min first time) and launches on device.

---

## ⚙️ Important Build Commands

```bash
# Fast feedback (30s) — compile just one feature module
./gradlew :screen-vehicle:compileCommonMainKotlinMetadata

# Full Android app build (~2 min)
./gradlew :androidApp:assembleDebug

# Run Logcat to see crash logs
adb logcat | grep androidApp
```

---

## 🆘 If It Doesn't Work

| Problem | Fix |
|---------|-----|
| "No configuration" / can't find androidApp | Run `./gradlew clean`, then **File** → **Sync Now** in Android Studio |
| App crashes on launch | Check **Logcat** (bottom) for errors; common: can't reach backend API |
| "gradle: command not found" | Use `./gradlew` (with dot-slash) from the project folder |
| Device not showing up | `adb kill-server && adb start-server`, reconnect USB |

---

## 📚 Full Reference

See [`ANDROID_STUDIO_SETUP.md`](ANDROID_STUDIO_SETUP.md) for complete step-by-step guide.

See [`CLAUDE.md`](CLAUDE.md) for architecture & coding rules.
