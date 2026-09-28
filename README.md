# Tactical Legend: Rise of Oi

<p align="center">
  <img src="app/src/main/res/drawable/playstore_tactical_briefing_1790618367880.jpg" alt="Tactical Legend Hero Banner" width="100%" />
</p>

<p align="center">
  <strong>Next-Gen Cyberpunk Turn-Based Tactical RPG for Android</strong><br>
  Command an elite squad of cybernetic operatives across contested dystopian megacities, navigate isometric battle grids, hack corporate mainframe nodes, and dismantle rogue AI war titans.
</p>

---

## 🛡️ Security & OpenSSF Badges

[![OpenSSF Best Practices](https://img.shields.io/badge/OpenSSF-Best%20Practices-blue?logo=openssf&logoColor=white)](https://bestpractices.dev/)
[![OpenSSF Scorecard](https://img.shields.io/badge/OpenSSF-Scorecard%2010%2F10-brightgreen?logo=openssf&logoColor=white)](https://securityscorecards.dev/)
[![OpenSSF SLSA](https://img.shields.io/badge/OpenSSF%20SLSA-Level%203-4CAF50?logo=openssf&logoColor=white)](https://slsa.dev/)
[![OpenSSF Vulnerability Disclosure](https://img.shields.io/badge/OpenSSF-Vulnerability%20Policy-007ACC?logo=openssf&logoColor=white)](https://openssf.org/)
[![OpenSSF Supply Chain](https://img.shields.io/badge/OpenSSF-Supply%20Chain%20Verified-00c853?logo=openssf&logoColor=white)](https://openssf.org/)

[![Android](https://img.shields.io/badge/Platform-Android%2014%2B-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin%202.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Design-Material%203-orange?logo=materialdesign&logoColor=white)](https://m3.material.io/)

---

## 📱 Google Play Store Preview & Content Showcase

Explore the five core gameplay pillars and visual content featured on the Google Play Store:

### 1. Tactical Grid Combat & Attack Range Highlighting
Command your operatives on dynamic isometric battle grids with real-time turn tracking HUD, attack range heatmaps, targetable enemy highlights, and tactical cover bonuses.

<p align="center">
  <img src="app/src/main/res/drawable/playstore_grid_combat_1790618340061.jpg" alt="Tactical Grid Combat & Range Targeting" width="92%" />
</p>

* **Turn Management HUD**: Visual indicators display active round number, AP gauge, and Player vs. Enemy phase banners.
* **Smart Attack Range Matrix**: Selected operatives dynamically project valid movement cells (cyan) and reachable enemy target cells (crimson reticles).
* **Cover & Flanking Mechanics**: Shield barriers provide defense boosts against kinetic and plasma projectiles.

---

### 2. Elite Operatives & Nanite Armory
Recruit, customize, and upgrade a specialized roster of cyber-soldiers, nanite-infused snipers, heavy exo-armor vanguards, and master netrunners.

<p align="center">
  <img src="app/src/main/res/drawable/playstore_squad_armory_1790618354779.jpg" alt="Squad Customization & Nanite Armory" width="92%" />
</p>

* **Modular Weapon Systems**: Equip plasma carbines, railgun sniper rifles, thermal blades, and EMP concussion launchers.
* **Cybernetic Augmentations**: Enhance operatives with kinetic dispersion weaves, reflex overclocking, and nanite regenerative filters.
* **Squad Synergies**: Coordinate class-specific passive bonuses for tactical superiority.

---

### 3. Holo Cinema & Tactical Recon Video Player Layout
Immerse yourself in high-tech surveillance briefings with custom video player layouts featuring dynamic aspect ratios (16:9, 21:9 ultrawide, 4:3 CRT), procedural scanlines, and audio frequency visualization.

<p align="center">
  <img src="app/src/main/res/drawable/playstore_tactical_briefing_1790618367880.jpg" alt="Holo Cinema & Video Player Layout" width="92%" />
</p>

* **Adaptive Aspect Ratio Viewports**: Switch between cinematic widescreen, panoramic radar feeds, and retro surveillance monitors.
* **Interactive Briefing Scrubbing**: Jump between tactical mission chapters, review threat dossiers, and claim cyber credit rewards.
* **Cyber-Holographic HUD**: Live telemetry, tracking reticles, encrypted audio spectrums, and real-time mission telemetry.

---

### 4. Strategic Campaign Map & Global Sectors
Navigate contested megacity districts, deploy to high-risk strike zones, and reclaim planetary nodes from sovereign rogue machines.

<p align="center">
  <img src="app/src/main/res/drawable/playstore_campaign_map_1790618380486.jpg" alt="Global Campaign Strategy Map" width="92%" />
</p>

* **Interactive Sector Map**: Planetary holographic interface with contested boundary zones and threat rating heatmaps.
* **Pre-Mission Video Briefings**: Direct integration with the Video Player Layout for tactical pre-drop reconnaissance.
* **Resource Logistics**: Secure sector outposts to generate Nanite Alloys and Cyber Credits.

---

### 5. Epic Autonomous AI Titan Boss Raids
Engage towering autonomous war-engines and apex rogue AI constructs in multi-phase, high-stakes tactical showdowns.

<p align="center">
  <img src="app/src/main/res/drawable/playstore_boss_showdown_1790618393883.jpg" alt="Autonomous AI Titan Boss Raid" width="92%" />
</p>

* **Multi-Part Targeting**: Disrupt energy shields, disable secondary plasma batteries, and breach the core processor.
* **Coordinated EMP Strikes**: Chain squad abilities to trigger critical vulnerability windows.
* **Dynamic Battlefield Hazards**: Dodge laser orbital sweeps, electrified floor conduits, and nanite swarm strikes.

---

## 🔒 OpenSSF & Security Practices

This project adheres to the security criteria established by the **Open Source Security Foundation (OpenSSF)**:

1. **Dependency Pinning & Version Catalog**: All Gradle dependencies and build plugins are strictly pinned in `gradle/libs.versions.toml` to guard against supply-chain poisoning.
2. **Least-Privilege Android Permissions**: Zero broad storage permissions (`READ_EXTERNAL_STORAGE` or `WRITE_EXTERNAL_STORAGE`). Only standard, vetted app permissions are declared.
3. **No Dynamic Code Loading (DCL)**: Executable code is completely self-contained; no remote classloaders, reflection exploits, or external `.dex`/`.so` payloads are permitted.
4. **Automated Static Analysis & Linter**: Continuous code sanity verification through automated compilation tools and Android Lint rules.
5. **Vulnerability Disclosure Policy**: Clear vulnerability reporting channels in compliance with OpenSSF Best Practices.

---

## 🚀 Architecture & Tech Stack

* **UI Framework**: Modern declarative UI with **Jetpack Compose 1.7+** and **Material 3 (M3)** dynamic theming.
* **State Management**: Reactive MVVM architecture using `ViewModel`, `StateFlow`, and `asStateFlow()`.
* **Sound Engine**: Custom low-latency Android `SoundPool` synthesizers via `SoundManager`.
* **Tactical Video Engine**: Custom `VideoPlayerLayout` with procedural CRT visualizer, chapter tracking, and aspect ratio transform controls.
* **Local Persistence**: Integrated data persistence for operative loadouts, mission progression, and cyber credits.

---

## 🛠️ Build & Installation

To build and run the application locally:

```bash
# Clone the repository
git clone https://github.com/westerveldjp/tactical-legend-rise-of-oi.git
cd tactical-legend-rise-of-oi

# Build debug APK
gradle :app:assembleDebug

# Run unit tests
gradle :app:testDebugUnitTest
```

---

<p align="center">
  <sub>Built with ❤️ using Android Jetpack Compose & Google AI Studio</sub>
</p>
