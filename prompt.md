# Cat Coinflip: full build prompt for agy

Build a free, open-source Fabric client mod called "Cat Coinflip" (mod id: catcoinflip, author: Itz0Cat). It adds extra stats to the tooltip of coinflip listings in server GUIs, a /cf quick-create shortcut, and a HUD later.

Work in this order and tell me when you finish each step: (1) rename and project scaffold, (2) assets, (3) number formatter and amount parser with tests, (4) data tracking and profiles, (5) tooltip, (6) quick create command, (7) config screen, (8) remote config, (9) auto-updater, (10) packaging and GitHub Actions. If something is blocked or ambiguous, ask me. Do not guess file paths or APIs.

---

## NAMING (apply everywhere before generating any files)
- Display name: "Cat Coinflip"
- Mod id: catcoinflip
- Java package: com.itz0cat.catcoinflip
- Client command root: /catcf (so /catcf resetstats and /catcf guiinfo). Keep /cf as the server's own command and the quick-create shortcut.
- Assets namespace: assets/catcoinflip/
- Config folder: config/catcoinflip/ (config.json, cache/, stats.json)
- Mixin config: catcoinflip.mixins.json
- Keybind category and Cloth Config title: "Cat Coinflip"
- Log prefix: [CatCoinflip]
- GitHub repo name: cat-coinflip
- Jar names: cat-coinflip-<version>+mc<minecraft version>.jar, checksums file: checksums.txt
- Rename the README title, GitHub Actions workflow names and unit test packages to match.

## TARGETS
- Minecraft 1.21.11 AND 26.1 (multi-version Gradle setup, one jar per version). 26.1 is unobfuscated, so check the current Fabric docs for the right Loom setup.
- Fabric Loader: no upper bound, minimum as low as works (>=0.15). Use Fabric API.
- Cloth Config is a required dependency (declare it in fabric.mod.json "depends"). Mod Menu is optional (suggests) and used for the config screen entrypoint.
- Client-side only ("environment": "client").

## ASSETS (source files already on my device)
- /sdcard/Download/cf.jpg = the mod icon (a pixel-art gold coin on a dark rounded square).
- /sdcard/Download/itz0cat_logo_clean.png = the Itz0Cat wordmark logo (blue-outlined white pixel text with cat ears, real transparency).
- Before anything else, copy both into the project: `mkdir -p assets-src && cp /sdcard/Download/cf.jpg /sdcard/Download/itz0cat_logo_clean.png assets-src/`. If /sdcard isn't visible inside the Debian proot, tell me and I'll copy them manually. Don't guess other paths.
- Never edit or recompress the originals. Keep them in assets-src/ and generate everything else from them with tools/make_assets.py (Pillow), so the assets can be rebuilt.

**Icon (from cf.jpg)**
- Output: src/main/resources/assets/catcoinflip/icon.png, 128x128 PNG.
- Center-crop to a square if needed, then resize with nearest-neighbor only (no smoothing). Convert to RGBA.
- cf.jpg is a JPEG, so it may have compression noise around the edges. Don't try to clean it up. Just report if the result looks noisy.
- fabric.mod.json "icon" points at this file.

**Logo (from itz0cat_logo_clean.png)**
- Copy it unchanged to assets/catcoinflip/textures/gui/itz0cat_logo.png. Don't re-export or resize the file itself.
- Draw it only at integer scales (1x, 2x, 3x...) with nearest-neighbor filtering, no blur.
- Use 1: a banner at the top of the Cloth Config screen, centered above the tabs, auto-picking the largest integer scale that fits.
- Use 2: an optional watermark that appears ONLY while the coinflip menu is open. Never in-world, never in other GUIs, never on the title screen.
  - Detection, per profile: the open screen's title (Text.getString(), formatting stripped) must match the profile's watermarkScreens list. Default for BananaSMP: ["Coinflip \\| Menu"].
  - Fallback signature, in case the title is styled differently: the container has 36 slots AND slots 30/31/32/35 are named PREVIOUS PAGE / STATISTICS / NEXT PAGE / CREATE GAME. Make the fallback toggleable (watermarkUseSignature, default true).
  - Draw it in the screen's render hook, after the background and before tooltips, so it never covers the tooltip or blocks clicks.
  - Position: anchored to the screen's bottom-right by default (config: watermarkAnchor = bottom_right | top_center_above_gui | bottom_center), with watermarkScale (integer), watermarkOpacity, and showWatermark (default false).
  - Add a client-side debug command /catcf guiinfo that prints, in local chat only, the current screen's title, slot count, and the names of the items in slots 0-3 and 30-35. I'll use it to find the titles of the Create Game and Statistics sub-menus, then add them to watermarkScreens. It sends no packets and clicks nothing.
- Use 3: the README header image.
- Mod Menu only takes a square icon, so the wordmark is not used there. The coin is.

**Check**
- After generating, print each asset's path and pixel size. icon.png must be 128x128, and itz0cat_logo.png must be the same size as the source.

## TOOLTIP
- Hook the item tooltip event. If the open screen title matches the active profile's guiTitleRegex and the item is a listing, parse the listing from its lore using the profile's regexes: player name (item name), amount.
- Render the configured lines in the user's order, between the Amount line and the "CLICK to Play" line. Every line has an id, a label template, a color, an icon, and an enabled flag.
- Lines: amount_short, if_win, if_lose, payout, house_cut, risk, can_afford, shortfall, balance_after_win, balance_after_loss, losses_to_broke, ev, breakeven_rate, win_rate, record, games_seen, streak, last_result, total_wagered, biggest_bet, avg_bet, net_profit, last_seen, vs_record, vs_profit, vs_last_result, rank, page_pos, size_vs_avg, listing_age, total_listings, whale_tag, session_profit, session_wl, session_winrate, my_streak, best_streak, worst_streak, biggest_win, biggest_loss, games_per_hour, profit_per_hour, risk_bar.
- A line with no data (e.g. win_rate for an unseen player) shows N/A in gray, or is hidden (a config option).
- Number formatting: below 100,000 show full numbers with commas. From 100,000 up use capital K, M, B, T, Qa with up to 2 decimals and trailing zeros trimmed (22B, 1.23M, 150K). Never show 1000K, it must roll over to 1M. The threshold (shortNumbersFrom) is configurable, and the Amount line can keep the server's full number.
- Default icons must be Unifont-safe symbols only: ★ ♛ ▲ ▼ ⚠ ✔ ✘ ⚔ ⚡ ⌛ ⚀-⚅ ♥ ↗ ↘ █ ░. Icons are configurable per line. Add a plain-text fallback option.

## DATA TRACKING (local only)
- Parse chat using the profile's regexes: the game summary (winner, loser, amount bet), the balance line, and created/rejected messages. Store per-player stats (W/L, wagered, biggest bet, streak, last seen) and the user's own session stats in a JSON file in the config folder. Add a command /catcf resetstats.
- Compute payout and house cut from profile.houseCut (default 5%, payout 1.9x).

## PROFILES
- Multiple profiles in one config (create, duplicate, rename, delete, switch). Each profile = one server's setup: serverAddressPatterns (auto-select the profile by server IP), guiTitleRegex, listing lore regexes, chat regexes, houseCut, minBet, currency symbol, commandAliases, watermarkScreens, line order, enabled lines, colors, icons, message templates.
- Ship a preconfigured profile "BananaSMP" (play.bananasmp.net) and a "Generic" profile. Server details from a real log:
  - GUI title: "Coinflip | Menu" (36 slots). Listings are player heads in slots 0 and up, with lore "Coinflip Game | $ Amount: $22,000,000,000 | ▶ CLICK to Play". Bottom row: slot 30 PREVIOUS PAGE, 31 STATISTICS, 32 NEXT PAGE, 35 CREATE GAME.
  - Result chat: "GAME SUMMARY", then "WINNER" or "LOSER", then "● Amount Bet: $190", "● Winner: <name>", "● Loser: <name>". The bet shown is the full pot (a $100 listing shows $190), and the winner nets 0.9x.
  - Balance line: "Your Balance: $101,276,265.69."
  - Create message: "COINFLIP ▶ Created Coinflip game for 100 (Money)!"
  - Create rejected: "The minimum bet is $100 for a coinflip!"
  - Commands: /coinflip (or /cf) opens the GUI, /cf create <amount> [money, shards], /cf delete, /cf toggle. Ignore shards entirely, track money only.

## QUICK CREATE COMMAND (/cf 28m)
- Goal: typing "/cf 28m" sends "/cf create 28000000". No "create", no zeros, no commas.
- How: rewrite the command the user just typed, using Fabric API ClientSendMessageEvents.MODIFY_COMMAND (or ALLOW_COMMAND plus a re-send if the version needs it). One typed command = one sent command. The mod never sends anything the user didn't type.
- Which commands: the profile's commandAliases (default for BananaSMP: ["cf", "coinflip"]). Rewrite only when the first argument is an amount, not a known subcommand (create, delete, toggle, help, stats).
  - "/cf 28m" -> "/cf create 28000000"
  - "/cf 1.5b" -> "/cf create 1500000000"
  - "/cf create 2.5m" -> "/cf create 2500000" (expands inside the normal command too)
  - "/cf 28m shards" -> "/cf create 28000000 shards" (extra arguments pass through untouched)
  - "/cf", "/cf delete", "/cf toggle", "/cf help" stay exactly as typed.
- Amount parser (its own class, with tests):
  - Accepted forms: 28m, 1.5m, .5m, 28M, 1,500,000, 1_500_000, 150k, 2b, 1t, 3qa, 28000000 (a plain number passes through).
  - Suffixes (case-insensitive, configurable in the profile): k=1e3, m=1e6, b=1e9, t=1e12, qa=1e15.
  - Use BigDecimal, never float or double, so 1.1m is exactly 1100000.
  - The result must be a whole number. If it isn't (0.0001k, 1.5 with no suffix), do NOT rewrite. Show a local-only error instead.
  - Reject zero, negatives, garbage and anything over Long.MAX_VALUE. On rejection, send nothing and show a local-only message with the reason.
  - Output is plain digits: no commas, no decimal point, no exponent.
  - Below the profile's minBet (BananaSMP: 100), still send it, since the server replies with its own error. Optionally show a local warning.
- Local echo (config: echoExpansion, default true): before sending, show one local-only chat line like "-> /cf create 28,000,000 ($28M)". This catches typos like 28b vs 28m. Never send it to the server.
- Optional safety (config: confirmAbove, default off): if set to e.g. 1B, amounts at or above it need typing the same command again within 10 seconds to go through.
- Tab completion: when typing /cf, suggest 1k, 10k, 100k, 1m, 10m, 100m, 1b, plus the user's last 5 amounts (stored locally).
- Config (Cloth Config, General tab, per profile): quickCreate on/off (default on), commandAliases, suffix map, template ("{alias} create {amount}"), echoExpansion, confirmAbove. The template is data-only, so it can be fixed from the remote defaults if a server changes its command.
- Applies only when the active profile is selected for the current server (match by server address), so /cf on other servers is untouched.

## REMOTE CONFIG (default config from GitHub)
- The repo hosts defaults at /config/defaults.json (raw.githubusercontent.com/<owner>/cat-coinflip/main/config/defaults.json), with a schemaVersion and configVersion.
- On launch (and via a "Check for config updates" button) fetch it asynchronously with a timeout, never blocking the game. Cache it locally. If offline, use the cache, then the bundled copy.
- Merge rule: remote defaults never overwrite user edits. Keep user overrides in a separate layer. The user sees "Remote default changed: X" with a button to adopt it per profile or per field, and a "Reset to remote default" button.
- Remote config is DATA ONLY: message templates, regexes, line definitions, placeholders, new profiles. Never execute code from it. Validate against the schema, cap file size, ignore unknown fields, reject on error.
- Custom placeholders: line and message templates support {amount} {player} {win_rate} {profit} {balance} etc. Users or the remote config can define new placeholders as formulas over existing values (a small safe expression evaluator with + - * / % min max round only, no scripting).
- All user-visible messages and line labels are editable in the config and localizable.

## AUTO-UPDATER
- Check GitHub Releases for the mod (latest release matching the running MC version). Show a notification in the config screen (and optionally a toast on the title screen), never silent.
- With the user's confirmation, download the jar to a temp file, verify the SHA-256 listed in the release's checksums.txt, and stage it to replace the old jar on next game exit/restart (handle the Windows locked-file case with a small updater step). Keep the old jar as a backup and add a Rollback button.
- Toggles: checkForUpdates, autoDownload (default off). Only ever download from this mod's own GitHub repo.

## CONFIG SCREEN (Cloth Config)
- Tabs: General, Profiles, Tooltip Lines, Messages and Placeholders, Formatting, Remote and Updates.
- Tooltip Lines tab: a row per line with an on/off toggle, ▲▼ reorder buttons, color picker and icon field, plus a live preview tooltip at the top.
- The Itz0Cat logo banner sits at the top of the screen (see ASSETS).
- Hotkey (default unbound) to toggle the tooltip.

## PACKAGING
- fabric.mod.json: name "Cat Coinflip", id catcoinflip, authors Itz0Cat, contact homepage https://itz0cat.vercel.app, issues and sources from the repo, license MIT, icon assets/catcoinflip/icon.png.
- Mod Menu description: "Cat Coinflip: see exactly what a coinflip is worth before you click. Win rate, profit and loss, risk, rank and more, right in the tooltip, plus /cf 28m quick create. Fully configurable, with profiles per server, remote default updates and a built-in updater. 100% free. No automation. By Itz0Cat."
- Mod Menu links: Discord https://dsc.gg/itz0cat, Website https://itz0cat.vercel.app. Add a "Free" badge.
- Provide a README (with the logo header), a GitHub Actions workflow that builds both versions and attaches jars plus checksums.txt to releases, and the defaults.json with schema docs.

## QUALITY
- No chat spam, no automation, no packets of the mod's own. The mod only reads GUI/chat, adds display text, and rewrites the single command the user typed (quick create). One user input = at most one command sent, and the mod never sends commands on its own.
- Fail safe: any parse error hides only that line and logs once.
- Unit tests for the number formatter, amount parser, regex parsers and the stat math.

