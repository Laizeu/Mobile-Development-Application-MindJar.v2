# MindJar

MindJar is an Android mental wellness journaling app that helps users log emotions, reflect on their thoughts, and access mental health resources. Users can record how they're feeling, browse their journal history, view motivational images, watch curated wellness videos, and reach out to crisis hotlines — all with offline support.

---

## Features

- **Emotion Logging** — Select from Happy, Sad, Pressured, or Angry and write a short description
- **Journal Feed** — Browse past entries sorted by newest first; tap any entry to view details
- **Edit & Delete Entries** — Update or remove journal entries with confirmation dialogs
- **Cloud Sync** — Entries are backed up to Firestore and restored across devices
- **Hope Screen** — Image carousel loaded from Firebase Storage with Room caching
- **Videos Screen** — Curated YouTube wellness videos loaded from Firebase Realtime Database
- **Hotline Screen** — Crisis contact cards with tap-to-call, email, and Facebook links
- **Google Sign-In** — Sign in with email/password or Google account
- **Offline-First** — All screens serve Room cache instantly; Firebase refreshes in background
- **Background Sync** — WorkManager retries any unsynced journal entries automatically
- **Slide Menu & Logout** — Animated side panel on Home screen with logout confirmation
- **Portrait & Landscape** — All screens have dedicated layout variants

---

## Tech Stack

| Category | Technology |
|---|---|
| Language | Java |
| Architecture | MVVM (ViewModel + LiveData + Repository) |
| Authentication | Firebase Auth — email/password + Google Sign-In |
| Cloud Database | Cloud Firestore — journal entry backup & sync |
| Realtime Database | Firebase Realtime Database — videos, hotlines, hope images |
| Storage | Firebase Storage — hope screen images |
| Local Cache | Room (SQLite) — offline-first persistence |
| Navigation | Navigation Component — fragment back stack, nav graph |
| Background Sync | WorkManager — retry unsynced entries |
| Image Loading | Glide |
| UI | Material Design 3 |

---

## Architecture

The app follows Model-View-ViewModel (MVVM). 
```
Fragment / Activity
    └── ViewModel          (survives rotation, holds LiveData)
         └── Repository    (cache-first: Room → Firebase)
              ├── Room DAOs      (instant offline reads/writes)
              └── Firebase       (Auth / Realtime DB / Firestore)
```

**Cache-first pattern** — every screen serves the Room cache immediately for fast, offline-capable rendering. Firebase fetches run in the background.

**Dual-write for journal entries** — entries are written to Room first (with a UUID as `firestoreId`), then pushed to Firestore asynchronously. `SyncJournalWorker` retries any entries where `syncedToFirebase = false`.

---

## Project Structure

```
app/src/main/java/com/example/myapplication/
│
├── ui/
│   ├── MainActivity.java                  # Auth host activity
│   ├── Dashboard.java                     # App host with bottom navigation
│   ├── auth/
│   │   ├── LoginFragment.java
│   │   └── SignUpFragment.java
│   ├── home/
│   │   ├── HomeFragment.java              # Emotion logging + slide menu
│   │   └── HomeViewModel.java
│   ├── realization/
│   │   ├── RealizationFragment.java       # Journal feed
│   │   ├── RealizationViewModel.java
│   │   ├── EntryDetailsFragment.java      # View single entry
│   │   ├── EntryDetailsViewModel.java
│   │   ├── EditEntryFragment.java         # Edit entry
│   │   └── EditEntryViewModel.java
│   ├── hope/
│   │   ├── HopeFragment.java              # Image carousel
│   │   └── HopeViewModel.java
│   ├── videos/
│   │   ├── VideosFragment.java
│   │   ├── VideoViewModel.java
│   │   └── VideoAdapter.java
│   └── hotline/
│       ├── HotlineFragment.java
│       ├── HotlineViewModel.java
│       └── HotlineAdapter.java
│
├── data/
│   ├── SessionManager.java                # Firebase UID access, logout
│   └── repository/
│       ├── AuthRepository.java
│       ├── JournalRepository.java         # Room + Firestore dual-write
│       ├── HopeRepository.java
│       ├── VideoRepository.java
│       └── HotlineRepository.java
│
├── data/local/
│   ├── AppDatabase.java                   # Room database (v12)
│   ├── AppExecutors.java
│   ├── dao/
│   │   ├── JournalEntryDao.java
│   │   ├── VideoDao.java
│   │   └── HotlineDao.java
│   └── entity/
│       ├── JournalEntryEntity.java
│       ├── VideoEntity.java
│       ├── HotlineEntity.java
│       └── HotlineEntry.java
│
└── util/
    └── SyncJournalWorker.java             # WorkManager background sync
```

---

## Setup & Run

### Our Setup

- Android Studio (latest stable)
- Android SDK configured
- A Firebase project with the following services enabled:
  - Firebase Authentication (Email/Password + Google)
  - Cloud Firestore
  - Firebase Realtime Database
  - Firebase Storage
- `google-services.json` placed in `app/`

### Steps

1. Clone the repository
   ```bash
   git clone https://github.com/your-username/MindJar.git
   ```
2. Open the project in Android Studio
3. Add `google-services.json` to the `app/` directory
4. Let Gradle sync and finish building
5. Connect an Android device or start an emulator (API 26+)
6. Click **Run ▶️**

---

## Firebase Data Structure

```
Realtime Database
├── hope_images/
│   └── image_1/  { url, order }
├── videos/
│   └── vid_1/    { videoId, title, order }
└── hotlines/
    └── entry_1/  { name, phone, email, facebookUrl, order }

Firestore
└── journal_entries/
    └── {userId}/
        └── entries/
            └── {firestoreId}/  { emotion, description, createdAtEpochMs, firestoreId }
```

> **Note:** Hope screen images stored in Firebase Storage while video URL are store in Realtime Database

---
