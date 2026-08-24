# Invento — Product Requirements Document (PRD)

**Document type:** Product Requirements Document  
**Product:** Invento (Device Lending Management System)  
**Version:** 1.0.0  
**Platform:** Offline Windows desktop (JavaFX)  
**Audience:** ICT laboratory administrators / lab staff  
**Status:** Implemented (as-built PRD for reference / IDE context)

---

## 1. Executive Summary

Invento is a professional **offline Windows desktop application** for managing ICT laboratory equipment borrowing and returns. Lab staff sign in at a front desk, manage inventory, record multi-device borrows, process returns with borrower verification, print thermal-style receipts, export reports, and back up the local database — all without internet.

**Tagline:** BORROW · TRACK · RETURN  
**Category:** ICT Laboratory Device Lending Management System

---

## 2. Problem Statement

School ICT labs lend keyboards, mice, headsets, laptops, chargers, cables, projectors, and similar equipment daily. Paper logs are error-prone; stock levels go out of sync; there is no reliable audit trail or printable proof of borrow/return. Invento solves this with a single-admin, offline desktop system tailored to lab desk workflows.

---

## 3. Goals & Non-Goals

### Goals
- Record borrows and returns accurately against live inventory stock
- Keep all data offline on the lab PC (SQLite + local files)
- Provide printable/PDF receipts for laboratory records
- Support inventory CRUD with optional device photos
- Provide dashboard KPIs, history search, Excel/PDF export, backup/restore
- Enforce secure admin login with forced change of default password
- Deliver a polished desktop UX (splash, themed UI, animated receipts)

### Non-Goals (out of scope for v1.0)
- Borrower self-service portal / mobile app
- Multi-admin roles or permission tiers
- Cloud sync, email/SMS notifications
- Due dates, overdue tracking, fines
- Multi-school / multi-tenant deployment
- Barcode/QR hardware integration (not required in current build)

---

## 4. Users & Personas

| Persona | Access | Description |
|---------|--------|-------------|
| **Lab Admin** | Signs into Invento | ICT lab technician/staff who manages inventory and processes front-desk borrows/returns |
| **Borrower** | No login | Student, Teacher, or Staff whose details are captured on borrow/return forms |

**Default credentials (first run):** username `admin`, temporary password `admin123` (must be changed before using the system).

---

## 5. Product Principles

1. **Offline-first** — no network dependency for core workflows  
2. **Front-desk speed** — POS-style booking, clear return flow, one-click receipts  
3. **Stock integrity** — borrow/return update available quantities in DB transactions  
4. **Auditability** — key admin actions logged  
5. **School context** — grade, section, position, purpose fields  
6. **Recoverability** — daily auto-backup + manual backup/restore  

---

## 6. Functional Requirements

### 6.1 Application Launch & Auth

| ID | Requirement |
|----|-------------|
| FR-AUTH-01 | Show branded splash animation on startup while DB initializes and daily backup check runs |
| FR-AUTH-02 | Present staff sign-in (username + password) after splash |
| FR-AUTH-03 | Authenticate using salted SHA-256 password hashes |
| FR-AUTH-04 | Force password change if still using default `admin123` (blocking dialog) |
| FR-AUTH-05 | New password minimum 6 characters; cannot be `admin123` |
| FR-AUTH-06 | Sign out clears session and returns to login; log `LOGIN` / `LOGOUT` |

### 6.2 Shell & Navigation

| ID | Requirement |
|----|-------------|
| FR-SHELL-01 | Main shell with sidebar: Dashboard, Inventory, Booking, Return, History, Settings |
| FR-SHELL-02 | Show signed-in username; light/dark theme toggle; sign out |
| FR-SHELL-03 | Minimum window size approximately 1100×700 |
| FR-SHELL-04 | Persist theme preference across sessions |

### 6.3 Dashboard

| ID | Requirement |
|----|-------------|
| FR-DASH-01 | Show time-based greeting and ICT Laboratory desk branding |
| FR-DASH-02 | Display KPI cards (e.g. device totals, borrowed units, returned today) |
| FR-DASH-03 | Show recent activity / recent transactions table |

### 6.4 Inventory

| ID | Requirement |
|----|-------------|
| FR-INV-01 | List devices in Photos (tile) or List mode |
| FR-INV-02 | Search by name/brand/serial; filter by category |
| FR-INV-03 | Add device: name, category, brand, optional serial, quantity, optional photo |
| FR-INV-04 | Edit device; quantity cannot drop below currently borrowed count |
| FR-INV-05 | Delete device only if no active borrows |
| FR-INV-06 | Auto-compute status: Available, Low Stock (≤25% available), Out of Stock |
| FR-INV-07 | Store photos under local `data/device-images/` |

### 6.5 Booking (Borrow)

| ID | Requirement |
|----|-------------|
| FR-BOR-01 | POS-style device catalog with search and category filter |
| FR-BOR-02 | Add items to borrow slip basket; quantity spinners respect stock |
| FR-BOR-03 | Capture borrower: full name, position (Student/Teacher/Staff), grade/dept, section, purpose |
| FR-BOR-04 | Support multi-device checkout in one borrow action |
| FR-BOR-05 | Generate transaction IDs `TXN-yyyyMMdd-####` |
| FR-BOR-06 | Decrement available stock; set status Borrowed |
| FR-BOR-07 | Offer animated thermal receipt; archive PDF under `data/exports/` |

### 6.6 Return

| ID | Requirement |
|----|-------------|
| FR-RET-01 | List active borrows (status = Borrowed) with search |
| FR-RET-02 | Verify borrower identity (name, position, grade/dept, section) before return |
| FR-RET-03 | Mark transaction Returned with date/time; restore inventory (capped at total quantity) |
| FR-RET-04 | Offer return receipt print / PDF archive |

### 6.7 History & Exports

| ID | Requirement |
|----|-------------|
| FR-HIS-01 | Full transaction history with text search and status filter (All/Borrowed/Returned) |
| FR-HIS-02 | Export Excel to `data/exports/transactions_<timestamp>.xlsx` |
| FR-HIS-03 | Export PDF tabular report |
| FR-HIS-04 | Reprint receipt for selected transaction |

### 6.8 Settings, Backup, Audit

| ID | Requirement |
|----|-------------|
| FR-SET-01 | Change password (current + new + confirm) |
| FR-SET-02 | Toggle theme |
| FR-SET-03 | Manual database backup to `data/backups/` |
| FR-SET-04 | Auto-backup on startup if no backup for today exists |
| FR-SET-05 | Restore from `.db` file with pre-restore safety copy |
| FR-SET-06 | Show recent audit log entries (e.g. last 50) |

### 6.9 Audit Trail Actions

Log at least: `LOGIN`, `LOGOUT`, `PASSWORD_CHANGE`, `BORROW`, `RETURN`, `DEVICE_ADD`, `DEVICE_UPDATE`, `DEVICE_DELETE`, `BACKUP`, `RESTORE`, `EXPORT_EXCEL`, `EXPORT_PDF`, `PRINT_RECEIPT`.

---

## 7. Data Model

### Entities

**Admin** — `admin_id`, `username`, `password_hash`, `salt`, `updated_at`

**Device** — `device_id`, `device_name`, `category`, `brand`, `serial_number`, `quantity`, `available_quantity`, `status`, `date_added`, `image_path`

**Borrower** — `borrower_id`, `full_name`, `position`, `grade_level`, `section`, `purpose`

**Transaction** — `transaction_id` (string PK), `borrower_id`, `device_id`, `quantity`, `borrow_date`, `borrow_time`, `return_date`, `return_time`, `status` (`Borrowed` | `Returned`)

**AuditLog** — `log_id`, `action`, `details`, `created_at`

### Relationships

```
Device ←── Transaction ──→ Borrower
Admin (standalone)
AuditLog (standalone)
```

One borrower record per checkout; multi-device borrow creates multiple transactions sharing the same borrower. SQLite foreign keys enabled.

### Seed data (first run)
Sample categories/devices such as Keyboard, Mouse, Headset, Laptop, Charger, Cable, Projector, Extension, Storage for demo/training.

---

## 8. Technical Architecture

| Layer | Choice |
|-------|--------|
| Language | Java 21 |
| UI | JavaFX 21 (programmatic views) |
| Pattern | MVC: view → controller → service → DAO → SQLite |
| Database | SQLite (`./data/lending.db`) |
| Build | Maven / Maven Wrapper (`mvnw.cmd`) |
| PDF | OpenHTMLToPDF + `templates/receipt.html` |
| Excel | Apache POI |
| Auth crypto | SHA-256 + random salt |
| Theme | `app.css` / `dark.css` + Java Preferences |

### Key packages
- `com.ict.lending.Main` — entry, splash, init
- `view` — UI screens and dialogs
- `controller` — thin screen controllers
- `service` — business logic
- `database` — connection, schema, DAOs
- `model` — domain objects
- `utils` — paths, hashing, theme, IDs, images

### Local data directories
```
./data/lending.db
./data/backups/
./data/exports/
./data/device-images/
```
Fallback path when needed: `%APPDATA%/ICTLending`

### Launch
`Invento.bat`, `run.bat`, or `mvnw.cmd javafx:run`

---

## 9. UX / UI Requirements

| Area | Requirement |
|------|-------------|
| Splash | Branded Invento logo expand / blue fill animation |
| Login | Full-height white panel flush to right; slides in from far right; animated background bubbles/rings; brand copy on left |
| Theme | Light and dark modes |
| Booking | Counter/POS catalog experience |
| Receipts | Animated thermal printer feed with rounded paper corners; Save PDF + Done |
| Transitions | Subtle page fades; polished dialogs |

---

## 10. Non-Functional Requirements

| ID | Category | Requirement |
|----|----------|-------------|
| NFR-01 | Offline | Core features work with no network |
| NFR-02 | Platform | Windows desktop primary target |
| NFR-03 | Reliability | Borrow/return in DB transactions with rollback on failure |
| NFR-04 | Security | Admin-only access; forced default password change; audit log |
| NFR-05 | Portability | Prefer project-relative `./data/` |
| NFR-06 | Recoverability | Daily auto-backup + manual backup/restore with safety copy |
| NFR-07 | Usability | Clear labels for school lab (grade, section, purpose) |
| NFR-08 | Performance | Splash + background DB init so UI appears promptly |

---

## 11. Core User Flows

### Borrow
1. Admin signs in → Booking  
2. Add device(s) to slip → Record borrow  
3. Fill borrower form → Confirm  
4. Stock updates; optional receipt animation + PDF archive  

### Return
1. Return module → select active borrow  
2. Verify borrower fields → Process return  
3. Stock restored; optional receipt  

### Inventory
1. Inventory → Add/Edit/Delete (with photo optional)  
2. Filters/search update catalog  

### Backup
1. Startup auto-backup if needed  
2. Settings → Backup or Restore `.db`  

---

## 12. Acceptance Criteria (v1.0)

- [ ] Fresh install creates DB, seeds admin + sample devices  
- [ ] First login forces password change away from `admin123`  
- [ ] Borrow reduces available stock; return restores it  
- [ ] Multi-device borrow creates linked transactions  
- [ ] Return rejects mismatched borrower verification  
- [ ] Receipt PDF is generated under `data/exports/`  
- [ ] Excel and PDF history exports succeed  
- [ ] Backup file appears in `data/backups/`; restore replaces DB safely  
- [ ] Light/dark theme persists after restart  
- [ ] App works fully offline on Windows  

---

## 13. Risks & Constraints

- Single-admin model: shared desk PC, not multi-user RBAC  
- Local SQLite: machine failure without backup loses data — mitigate via auto-backup  
- JavaFX runtime / JDK 21+ required to run  
- No overdue/fine logic — lab policy remains manual  

---

## 14. Glossary

| Term | Meaning |
|------|---------|
| Invento | Product name of the lending system |
| Booking | Sidebar name for the borrow / checkout module |
| Transaction | One borrow/return record for a device line (`TXN-…`) |
| Available quantity | Units currently on shelf (not borrowed) |
| Thermal receipt | On-screen animated receipt + archived PDF |

---

## 15. Document Use (Kiro IDE)

Paste or attach this PRD in Kiro as product context when asking the IDE to:
- Explain Invento behavior  
- Propose features that fit the existing architecture  
- Generate tests, docs, or incremental improvements  

**Important:** This PRD describes the **as-built** Invento application. Do not treat unimplemented “nice-to-haves” from Non-Goals as existing features unless explicitly requested.

---

*End of Invento PRD v1.0*
