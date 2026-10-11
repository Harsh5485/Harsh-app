# Harsh TV - Premium Android TV Movies & Series Streaming

<p align="center">
  <img src="art/banner.svg" alt="Harsh TV Banner" width="100%" />
</p>

<p align="center">
  <a href="https://github.com/Harsh5485/Harsh-app/releases"><img src="https://img.shields.io/badge/Download-Harsh_TV_APK-22c55e?style=for-the-badge&logo=android&logoColor=white" alt="Download APK" /></a>
  &nbsp;
  <a href="https://github.com/Harsh5485/Harsh-app/archive/refs/heads/main.zip"><img src="https://img.shields.io/badge/Download-Source_ZIP-3b82f6?style=for-the-badge&logo=github&logoColor=white" alt="Download Source Code" /></a>
</p>

---

## 📺 About Harsh TV

**Harsh TV** is a professional Android TV (Leanback compatible) streaming application for movies and series. Fully optimized for large screens, landscape orientation, and 100% controllable via standard Android TV D-Pad remotes.

---

## 🚀 Key Android TV & Player Features

- **🎯 100% D-Pad Remote Navigation**: Every element (cards, buttons, side drawer, player) features high-contrast glowing focus borders and 1.08x pop-up scaling.
- **📺 Android TV Leanback Support**: Declared `<uses-feature android:name="android.software.leanback" />` and Leanback launcher banner (`tv_banner.xml`).
- **🛡️ TV Overscan Safe Margins**: 24.dp TV-safe padding so UI elements never get clipped by physical TV borders.
- **⚡ ExoPlayer D-Pad Remote Control**:
  - **Left / Right**: Seek 10s backward / forward with visual feedback.
  - **Center OK**: Toggle Play / Pause instantly.
  - **Up / Down**: Open Audio/Subtitle tracks and Stream/Episode selector.
  - **Auto-Hide**: Player controls automatically hide after 3 seconds of remote inactivity.
- **🔄 Codec & Stream Resilience**: Software decoder fallback for HEVC/4K on low-end TV hardware and complete player state resets on resolution switches.

---

## 📸 App Screenshots

<p align="center">
  <img src="art/screenshot_home.svg" width="30%" alt="Home Screen" />
  &nbsp;&nbsp;
  <img src="art/screenshot_player.svg" width="30%" alt="Player View" />
  &nbsp;&nbsp;
  <img src="art/screenshot_detail.svg" width="30%" alt="Detail View" />
</p>

---

## 📥 Download Links

- 📦 **[Download APK (Releases)](https://github.com/Harsh5485/Harsh-app/releases)**
- 📁 **[Download Source Code (ZIP)](https://github.com/Harsh5485/Harsh-app/archive/refs/heads/main.zip)**

---

## ⚙️ How to generate the APK on GitHub Releases

Since your repository **`Harsh5485/Harsh-app`** currently has no published release:

1. **Option 1: Automatic Build via GitHub Actions**
   - Push this updated code from AI Studio to GitHub.
   - Go to the **Actions** tab in `https://github.com/Harsh5485/Harsh-app`.
   - The automated workflow (`.github/workflows/build-apk.yml`) will build `app-debug.apk` automatically and create the Release!

2. **Option 2: Create Release Manually**
   - Go to `https://github.com/Harsh5485/Harsh-app/releases`.
   - Click the green **"Create a new release"** button.
   - Tag version: `v1.0.0`
   - Release title: `Harsh App v1.0.0`
   - Attach the APK file (which you can generate from AI Studio top-right **Settings -> Generate APK** or from Android Studio `gradle assembleDebug`).
   - Click **Publish release**.

---

## 🛠️ Local Development

1. Clone the repository:
   ```bash
   git clone https://github.com/Harsh5485/Harsh-app.git
   cd Harsh-app
   ```
2. Open in **Android Studio**.
3. Build the APK locally using Gradle:
   ```bash
   gradle assembleDebug
   ```
   Output APK: `app/build/outputs/apk/debug/app-debug.apk`.

---

## 📄 License

This project is open-source and licensed under the [MIT License](LICENSE).
