<p align="center">
  <img src="assets/itz0cat_logo.png" alt="Itz0Cat Logo" width="400">
</p>

# Cat Coinflip 🐱🪙

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Build Status](https://github.com/itz0cat/cat-coinflip/actions/workflows/build.yml/badge.svg)](https://github.com/itz0cat/cat-coinflip/actions)
[![Discord](https://img.shields.io/badge/Discord-Join-7289DA?logo=discord&logoColor=white)](https://dsc.gg/itz0cat)
[![Website](https://img.shields.io/badge/Website-itz0cat.vercel.app-blue)](https://itz0cat.vercel.app)

**Cat Coinflip** is a free, open-source Fabric client mod designed for competitive Minecraft coinflip servers (such as BananaSMP). It provides real-time mathematical risk analytics directly on item tooltips, quick command shortcuts (`/cf 28m`), local statistics tracking, and server profile presets.

---

## ✨ Features

- 📊 **Dynamic Tooltip HUD**: Evaluates potential profit, loss, expected value (EV), breakeven win rate, opponent win rate, historical records, and streaks before you click.
- ⚡ **Quick Create Shortcut**: Type `/cf 28m` or `/cf 1.5b` to immediately execute `/cf create 28000000` without typing zeros or commas.
- 🛡️ **Safety Confirmation**: Optional confirmation threshold (e.g. bets $\ge$ 1B require re-typing within 10 seconds to confirm).
- 📈 **Local Player & Session Tracking**: Monitors win/loss records, total wagered, profit per hour, and head-to-head match stats stored in `config/catcoinflip/stats.json`.
- ⚙️ **Cloth Config GUI**: Clean tabbed settings menu with profiles, custom tooltip reordering, format thresholds, and remote config syncing.
- 🔄 **Safe Auto-Updater**: Checks GitHub Releases for updates matching your Minecraft version, verifies SHA-256 hashes against `checksums.txt`, and safely replaces JARs without lock issues using `BurnerCleanup`.

---

## 🎮 Commands

| Command | Description |
| :--- | :--- |
| `/catcf` | Opens the Cloth Config settings screen |
| `/catcf guiinfo` | Client debug tool: prints current container slots, names, and titles |
| `/catcf resetstats` | Resets local session and player tracking statistics |
| `/catcf status` | Shows current active server profile and status |
| `/cf <amount>` | Quick-create coinflip (e.g. `/cf 28m` expands to `/cf create 28000000`) |

---

## 📦 Supported Minecraft Versions

- **Minecraft 1.21.11** (Fabric Loader $\ge$ 0.15.0, Java 21)
- **Minecraft 26.1.2** (Fabric Loader $\ge$ 0.15.0, Java 25)

---

## 📄 License

This mod is free and open-source licensed under the [MIT License](LICENSE).
Created by **Itz0Cat**.
