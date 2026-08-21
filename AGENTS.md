# ZhuZhiLiao Spatial App

- Windows project: `D:\PICO\projects\zhuzhiliao`
- WSL editing path: `/mnt/d/PICO/projects/zhuzhiliao`
- Package/application ID: `com.example.zhuzhiliao`
- App label: `竹知了`; launcher icon source: `app/src/main/res/mipmap-anydpi/ic_spatial_launcher.png`
- Launcher: `.platform.LaunchActivity`
- Container: volumetric `DefaultWindowContainer`, Spatial SDK BOM `0.13.3`
- 2D UI must use `com.pico.spatial.ui.*` under `PicoTheme`; Material/Material3 is forbidden.
- Keep the window background transparent. Only the compact HUD is opaque for passthrough contrast.
- The toy is built from PICO primitive meshes. Do not add reference-project textures, models,
  Earth, leaderboard, account, server, or location features. The bundled sound is generated at
  runtime by `audio/WahSoundPlayer.kt`; no reference recording is copied.

## Key Files

- `domain/ZhuzhiliaoEngine.kt`: deterministic auto-spin state and counters.
- `scene/ZhuzhiliaoScene.kt`: primitive ECS toy, rope segments, wings, materials, and light.
- `physics/*`, `motion/*`: ports of the reference OrbitCounter, fixed-step ToySimulation,
  MotionSampleFilter, ShakeDrive, and ShakeGestureDetector. These contain behavior only; the
  reference OpenGL renderer and Earth/network/account modules remain excluded.
- `audio/WahSoundPlayer.kt`: short runtime-generated local wah chirp.
- `ui/ZhuzhiliaoHud.kt`: Chinese SpatialUI status and controls.
- `content/HomeVolume.kt`: frame loop, SpatialView, attachment, and lifecycle cleanup.

## Build And Run

All Gradle and PICO emulator commands run in Windows PowerShell with PICO payload/cache/temp paths
on `D:`. Never build while the PICO emulator is running. Use this order:

1. Stop emulator: `pico-cli emulator stop --avd Pico_Emulator_0_13 --format json`
2. Build: `.\gradlew.bat --no-daemon testDebugUnitTest assembleDebug`
3. Run the SpatialUI verifier and Material import scan from WSL.
4. Start the emulator, install `app\build\outputs\apk\debug\app-debug.apk`, clear logcat,
   launch explicitly with `.platform.LaunchActivity`, then inspect `adb logcat -b crash -d`.

Do not use automated screenshots or adb 2D coordinate taps to validate volumetric UI. Visual and
spatial-ray interaction acceptance should be performed in the PICO emulator window.

## Current HUD/runtime baseline

- HUD is a single horizontal `640x100dp` SpatialUI strip (`1280x200px` at the
  emulator density), rendered at scale `1.0` and placed at
  `Vector3(0.15f, -0.25f, 0.07f)` below/right of the toy.
- The volumetric container remains `1080x820x560dp`; runtime reports
  `2160x1640x1120px`, `style=2`, and `volume_base_panel=1`.
- The frame loop runs at 30Hz and skips identical idle-state ECS submissions.
  The procedural rope uses three primitive segments to reduce startup and
  per-frame compositor load while preserving endpoint tracking.
- Verified on `emulator-5554` after a fresh install/launch: process running,
  no fresh crash, no ANR during a 45-second observation, and Spatial FPS held
  near 60 after the initial compositor warm-up.
