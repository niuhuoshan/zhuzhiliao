# 竹知了

基于 PICO Spatial SDK 的空间竹知了玩具应用。应用使用 PICO primitive mesh
程序化生成竹筒、握柄、翅膀和绳索，并在空间窗口中提供自动甩动、停止、重置、
转速和计数反馈。音效是项目内的本地资源，不包含旧项目的其他功能或素材。

## 项目信息

- Application ID: `com.example.zhuzhiliao`
- PICO Spatial SDK BOM: `0.13.3`
- Window: volumetric
- ABI: `arm64-v8a`

## 构建

在 Windows PowerShell 中配置 Android Studio JBR、Android SDK 和 D 盘上的 PICO/Gradle
缓存后，先停止 PICO 模拟器，再执行：

```powershell
.\gradlew.bat --no-daemon testDebugUnitTest assembleDebug
```

生成的 debug APK 位于 `app\build\outputs\apk\debug\app-debug.apk`。`build/`、APK、
本机配置和缓存不会提交到 Git。

