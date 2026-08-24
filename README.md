# Invento — Windows Download

Offline Windows desktop app for managing ICT laboratory equipment borrowing and returns (device lending, inventory, receipts, reports). This repo packages **Invento** as a self-contained Windows distribution — no Java, no Maven, nothing to install separately on the machine that runs it.

## Get Invento (no setup required)

Go to the [Releases](../../releases) page and download one of these from the latest release:

- **`Invento-1.0.0.exe`** (installer, recommended) — double-click it, click through the install prompts. Installs to your user profile (no admin rights needed), adds a Start Menu shortcut, and includes an uninstaller in "Apps & features".
- **`Invento-Portable-Windows.zip`** (no install) — extract anywhere and double-click `Invento.exe` inside the extracted `Invento` folder. Good for USB drives or PCs where you don't want to install anything at all.

Either way, the app bundles its own Java runtime — it runs on any 64-bit Windows 10/11 PC with nothing pre-installed.

> **Do not** run `run.bat`, `Invento.bat`, or `mvnw.cmd` on a machine you're distributing to — those are developer scripts that require a JDK to be installed and will fail with a Java/JAVA_HOME error on a machine that doesn't have one. They're only for working on the source code (see below).

**Default login:** username `admin`, password `admin123` (you'll be required to set a new password on first sign-in).

### Sharing it yourself

- **Flash drive:** copy `Invento-1.0.0.exe` or the extracted portable `Invento` folder onto the drive. Plug it into another Windows PC and run it from there, or copy it locally first.
- **GitHub:** point people at this repo's [Releases](../../releases) page, or fork/clone the repo — a new build is produced automatically for every tagged release (see below).

All data (`data/lending.db`, backups, exports) is stored offline, next to wherever the app is installed/run from.

## Building the distributables yourself

Requirements: JDK 21+ (JDK 21/26 tested; must include `jpackage`), Windows, and the [WiX Toolset v3](https://wixtoolset.org/) (only needed for the `.exe` installer — `choco install wixtoolset`). Without WiX, the script still produces the portable build and just skips the installer.

```powershell
powershell -ExecutionPolicy Bypass -File .\build-portable.ps1
```

This runs `mvnw clean package` and then `jpackage` to produce:

- `dist/Invento-1.0.0.exe` — the Windows installer
- `dist/Invento/` — the runnable, self-contained app folder
- `dist/Invento-Portable-Windows.zip` — the same folder, zipped for distribution

### Automatic builds via GitHub Actions

Pushing a tag like `v1.0.0` triggers [`.github/workflows/release.yml`](.github/workflows/release.yml), which builds both distributables on a clean Windows runner (installing WiX automatically if needed) and attaches them to a GitHub Release:

```bash
git tag v1.0.0
git push origin v1.0.0
```

You can also trigger a build manually from the Actions tab (`workflow_dispatch`) without creating a release.

## Running from source (development)

```text
mvnw.cmd javafx:run
```

or double-click `run.bat`. Open the project folder (`pom.xml`) in IntelliJ IDEA / NetBeans and run `com.ict.lending.Launcher` (or `com.ict.lending.Main` directly, which the IDE can launch as a JavaFX app).

> `Launcher` exists alongside `Main` so the packaged/`java -jar` build can start correctly — the JVM refuses to launch a class that directly extends `javafx.application.Application` from a plain classpath jar. `Launcher` is a plain entry point that just calls `Main.main()`.

## Features

- Admin login with salted SHA-256 password hashing
- Dashboard KPIs (total, available, borrowed, returned today)
- Inventory CRUD with search and category filter
- Borrow flow with borrower form and stock validation
- Return flow with borrower verification
- Transaction history with search, filter, PDF/Excel export, and printable receipts
- Automatic daily database backup plus manual backup/restore
- Audit log of admin activities

## Project structure

```text
src/main/java/com/ict/lending/
  Main.java        # JavaFX Application entry point
  Launcher.java     # non-Application entry point used by the packaged build
  model/
  database/
  service/
  controller/
  view/
  utils/
src/main/resources/
  css/app.css
  templates/receipt.html
build-portable.ps1  # builds the installer + portable package
.github/workflows/release.yml  # CI: builds + publishes both on tag push
```

See [Invento_PRD.md](Invento_PRD.md) for the full product requirements document.
