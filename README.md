# 🧷 Clippr

**Clippr** is a minimalist clipboard manager built for macOS.  
It saves everything you copy on device and let's you paste them directly from the menu bar or the main app

---

## 🚀 Features

### 📋 Clipboard History
Automatically captures everything you copy. Quickly access your recent clips from the tray icon or main window.

### 📌 Pinned Clips
Keep your most important clips at the top for instant access.  
<img src="assets/pinned_clips.png" style="border-radius: 3%;"/>

### ✏️ Edit Clips
Edit your clip content and change associated type if incorrectly categorized 
<img src="assets/edit.png" style="border-radius: 3%;"/>

### 🔍 Search & Merge
Search your clipboard history, filter and select multiple clips to **merge** into one, **paste** or **copy**
<img src="assets/search_merge.png" style="border-radius: 3%;"/>

### ⚙️ Settings
Customize how Clippr behaves, when to delete clips, when to start copying, maximum clips to copy at a time...  
<img src="assets/settings_1.png" style="border-radius: 3%;"/>

### 🚫 Exceptions
Exclude certain apps or content types from being saved to help you control what Clippr saves
<img src="assets/settings_exceptions.png" style="border-radius: 3%;"/>

### 🧠 Tray Menu
Access your clips pinned clips, settings, and quick actions right from the macOS menu bar.  
<img src="assets/tray.png" style="border-radius: 3%;"/>

### ⚡️ Accessibility 
Clippr supports intuitive shortcuts in app that lets you access your clips without having to move those crusty hands to the mouse

---

### 🔒 Privacy

Your privacy is core to Clippr’s design.

🗄️ All clips are stored locally on your device — nothing leaves your computer.

❌ No cloud sync, telemetry, or analytics.

🔐 You control what gets saved and can clear history anytime.

### 🧰 Tech Stack

Kotlin (JVM)

JetBrains Compose Desktop

JNA interop

Room DB for storage

### 📜 License

MIT License © 2025 Phoenix Jatrix.
Feel free to modify with attribution.

---

## 🧩 Cloning & Running

```bash
# Clone the repository
git clone https://github.com/phoenixjatrix/clippr.git

# Open the project directory
cd clippr

# Build and run (Gradle)
./gradlew run