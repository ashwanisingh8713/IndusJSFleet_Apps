# Android Studio Setup — IndusJS Fleet KMP Apps

Complete guide to run the Android app from Android Studio's **Run** button.

---

## ✅ Prerequisites

- **JDK 17+** (required by the project; Android Studio bundles one, but verify)
- **Android Studio** (latest stable, with KMP plugin)
- **Android SDK** (API 36 target, API 23 min; Android Studio SDK Manager will guide)
- **local.properties** — ✓ already configured at project root with `sdk.dir`

---

## 🚀 Step 1: Open Project in Android Studio

1. In Android Studio, select **File** → **Open**
2. Navigate to: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps`
3. Click **Open**

Android Studio will begin indexing. Give it 2–3 minutes.

---

## 🔄 Step 2: Sync Gradle

After opening, you should see a **Gradle sync** notification at the top. If not, trigger manually:

- **File** → **Sync Now** (or press **⌘+⇧+I** on Mac)

Watch the **Build** tab at the bottom for completion. This downloads dependencies and configures all 28 modules.

**If sync fails:**
- Check that `local.properties` has a valid `sdk.dir` (should be `/Users/ashwani/Library/Android/sdk`)
- Ensure JDK 17+ is available: **Android Studio** → **Preferences** → **Build, Execution, Deployment** → **Gradle** → check JDK version
- Run `./gradlew clean` from the terminal in the `IndusJSFleet_Apps/` directory

---

## 🏃 Step 3: Create/Verify Run Configuration

The run configuration tells Android Studio how to build and launch the app.

### Option A: Use Auto-Detection (Recommended)

1. In the top toolbar, click the **Run configuration** dropdown (currently may say "No Configuration" or similar)
2. Select **androidApp** if it appears in the list
3. If not listed, click **Edit Configurations...**

### Option B: Create Run Configuration Manually

1. **Run** → **Edit Configurations...**
2. Click **+** (New) → **Android App**
3. Fill in:
   - **Name:** `androidApp`
   - **Module:** `IndusJSFleet.androidApp` (select from dropdown)
   - **Launch Options:**
     - Activity: leave default or select `com.indusjs.fleet.androidApp.AppActivity`
     - Launch: `Default Activity`
4. Click **OK**

---

## 📱 Step 4: Prepare a Device or Emulator

**Physical Device:**
- Connect Android phone via USB
- Enable **Developer Mode**: Settings → About Phone → tap Build Number 7 times
- Enable **USB Debugging**: Settings → Developer Options → USB Debugging
- Trust the computer when prompted

**Emulator:**
- **Tools** → **Device Manager**
- Click **Create Device**
- Choose a recent API level (e.g., API 33 or 34)
- Click **Play** to start the emulator
- Wait for it to fully boot

Verify the device is visible:
```bash
adb devices
```
You should see your device listed (physical or emulator).

---

## ▶️ Step 5: Run the App

1. Select your device from the device dropdown (top toolbar)
2. Click the **Run** button (▶️ green play icon) or press **⌃R** (Ctrl+R)

Android Studio will:
- Compile the `androidApp` module and its dependencies
- Build an APK
- Install it on the device/emulator
- Launch the app

**First build takes ~2–3 minutes** (Gradle, Kotlin compilation, etc.). Subsequent builds are faster.

---

## 🎯 Golden Rules While Editing

**Fast feedback during development:**
```bash
# Fastest: compile just the module you're editing
./gradlew :screen-vehicle:compileCommonMainKotlinMetadata

# Before pushing: full Android app build
./gradlew :androidApp:assembleDebug
```

**MVI discipline:** ViewModels extend `MviViewModel<State, Intent, Effect>`; never use `mutableStateOf`. Navigation goes through lambda callbacks, not effects.

**Network contract:** The app sends `Authorization: Bearer <JWT>` to `/api/v1` routes. The backend validates tokens. Don't bypass auth in the network layer.

---

## 🆘 Troubleshooting

| Issue | Solution |
|-------|----------|
| **"Module not found" or Gradle sync fails** | Run `./gradlew clean` in a terminal, then **File** → **Sync Now** again |
| **"No devices found"** | Check `adb devices` in terminal; restart adb (`adb kill-server && adb start-server`) |
| **App crashes on launch** | Check **Logcat** (bottom panel) for stack traces. Common: JWT auth failure → check backend is reachable |
| **Slow builds** | Gradle daemon is running; cached. First build is slow; next ones ~30s. Check `gradle.properties` has caching enabled |
| **"Plugin not found"** | JDK version mismatch. Verify JDK 17+ in Preferences → Gradle. Restart Android Studio. |
| **Kotlin metadata errors** | `.gradle` cache is corrupted. Run `./gradlew clean`, then sync again |

---

## 📖 Next Steps

- **Edit a feature:** Modules live in `screen-*` folders. See `CLAUDE.md` for architecture rules.
- **Run tests:** `./gradlew test` (or per-module: `./gradlew :screen-vehicle:test`)
- **Build for release:** `./gradlew :androidApp:assembleRelease` (requires signing keys)
- **Debug:** Add breakpoints in Android Studio, then click **Debug** (🐛) instead of **Run**

---

**For detailed architecture and conventions, see [`project_context.md`](project_context.md).**
