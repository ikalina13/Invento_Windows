# Invento — Portable Windows Download

Offline Windows desktop app for managing ICT laboratory equipment borrowing and returns (device lending, inventory, receipts, reports). This repo packages **Invento** as a self-contained, portable Windows app — no Java, no Maven, no installer required on the machine that runs it.

## Get Invento (no setup required)

1. Go to the [Releases](../../releases) page and download `Invento-Portable-Windows.zip` from the latest release.
2. Extract the zip anywhere (Desktop, a USB flash drive, a shared folder).
3. Open the extracted `Invento` folder and double-click `Invento.exe`.

That's it — the folder bundles its own Java runtime, so it runs on any 64-bit Windows 10/11 PC with nothing pre-installed.

**Default login:** username `admin`, password `admin123` (you'll be required to set a new password on first sign-in).

### Sharing it yourself

- **Flash drive:** copy the extracted `Invento` folder (or the zip) onto the drive. Plug it into another Windows PC, extract if needed, and run `Invento.exe` directly from the drive or after copying it locally.
- **GitHub:** point people at this repo's [Releases](../../releases) page, or fork/clone the repo — a new build is produced automatically for every tagged release (see below).

All data (`data/lending.db`, backups, exports) is stored inside the folder the app is run from, entirely offline.

## Building the portable package yourself

Requirements: JDK 21+ (JDK 21/26 tested; must include `jpackage`) and Windows.

```powershell
powershell -ExecutionPolicy Bypass -File .\build-portable.ps1
```

This runs `mvnw clean package` and then `jpackage` to produce:

- `dist/Invento/` — the runnable, self-contained app folder
- `dist/Invento-Portable-Windows.zip` — the same thing, zipped for distribution

### Automatic builds via GitHub Actions

Pushing a tag like `v1.0.0` triggers [`.github/workflows/release.yml`](.github/workflows/release.yml), which builds the portable package on a clean Windows runner and attaches the zip to a GitHub Release automatically:

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
build-portable.ps1  # builds the self-contained Windows package
.github/workflows/release.yml  # CI: builds + publishes the package on tag push
```

See [Invento_PRD.md](Invento_PRD.md) for the full product requirements document.
