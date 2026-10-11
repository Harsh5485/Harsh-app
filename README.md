# Harsh - Premium Movies & Series Streaming

<p align="center">
  <img src="art/banner.svg" alt="Harsh Banner" width="100%" />
</p>

---

## 📱 About The App

**Harsh** is a high-performance Android streaming application for movies and series. Built natively with Jetpack Compose and ExoPlayer, it features fast streaming and Telegram-Stremio source integration with smart codec fallback.

---

## 🚀 Key Features

- **⚡ Hardware & Software Fallback**: Automatically switches to software decoding for HEVC (H.265) / 4K videos if hardware acceleration fails on budget devices.
- **🔄 Complete State Reset**: Releases previous player instances and clears surface/codec state when switching streams (e.g. 4K to 1080p).
- **🛡️ Device Capability Detection**: Intelligently selects the most optimal stream (H.264 / 1080p) on lower-end devices to prevent player crashes.
- **📺 Telegram & Stremio Integration**: Access high-speed playback links across multiple sources.
- **🎨 Modern Material 3 UI**: Clean, responsive layout supporting phones and foldables.

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

## 📥 Download Options

### 1️⃣ Direct Download in AI Studio
- Click on the **Settings / Export** menu in the top right corner of AI Studio.
- Choose **Export Project as ZIP** (for Source Code) or **Generate APK**.

### 2️⃣ Download from GitHub Releases
Once you push this project to your GitHub repository:
- **GitHub Actions** will automatically build the APK.
- Go to the **Releases** tab of your GitHub repository or click below:
  - 📦 **[Download Latest APK](../../releases/latest)**
  - 📁 **[Download Source Code ZIP](../../archive/refs/heads/main.zip)**

---

## 🛠️ Build & Setup Instructions

### Local Development
1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/Harsh.git
   cd Harsh
   ```
2. Open the project in **Android Studio** (Jellyfish or newer).
3. Sync Gradle and run on an Android Device / Emulator (Android 7.0+ / API 24+).
4. Build the APK locally using Gradle:
   ```bash
   gradle assembleDebug
   ```
   The APK file will be generated at `app/build/outputs/apk/debug/app-debug.apk`.

---

## 📄 License

This project is open-source and licensed under the [MIT License](LICENSE).
