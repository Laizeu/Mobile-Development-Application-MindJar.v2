# MindJar

MindJar is an Android mental wellness journaling app that helps users log emotions, reflect on their thoughts, and access mental health resources. Users can record how they're feeling, browse their journal history, view motivational images, watch curated wellness videos, and reach out to crisis hotlines — all with offline support.

---

## Features

- **Emotion Logging** — Select from Happy, Sad, Pressured, or Angry and write a short description
- **Journal Feed** — Browse past entries sorted by newest first; tap any entry to view details
- **Edit & Delete Entries** — Update or remove journal entries with confirmation dialogs
- **Cloud Sync** — Entries are backed up to Firestore and restored across devices
- **Hope Screen** — Image carousel loaded from Firebase Storage
- **Videos Screen** — Curated YouTube wellness videos loaded from Firebase Realtime Database
- **Hotline Screen** — Crisis contact cards with tap-to-call, email, and Facebook links
- **Google Sign-In** — Sign in with email/password or Google account
- **Offline-First** — Journaling screens serve Room cache instantly; Firebase refreshes in background
- **Background Sync** — WorkManager retries any unsynced journal entries automatically
- **Slide Menu & Logout** — Animated side panel on Home screen with logout confirmation
- **User Profile** — View and edit display name, and add avatar
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
│   ├── MainActivity.java                  # Auth host activity (nav_graph: auth_nav_graph)
│   ├── Dashboard.java                     # App host with custom bottom navigation
│   │
│   ├── auth/
│   │   ├── LoginFragment.java             # Email/password + Google Sign-In, password reset
│   │   └── SignUpFragment.java            # Registration with live field validation
│   │
│   ├── home/
│   │   ├── HomeFragment.java              # Emotion logging + animated slide menu
│   │   └── HomeViewModel.java             # Saves journal entries via JournalRepository
│   │
│   ├── realization/
│   │   ├── RealizationFragment.java       # Journal feed (Room-only, refreshes on resume)
│   │   ├── RealizationViewModel.java      # loadEntries() + loadEntriesWithRestore()
│   │   ├── MyJourneyFragment.java         # Alternative full-list view 
│   │   ├── MyJourneyAdapter.java          # RecyclerView adapter for journal entry cards
│   │   ├── EntryDetailsFragment.java      # Read-only view of a single entry
│   │   ├── EntryDetailsViewModel.java     # Loads + deletes a single entry
│   │   ├── EditEntryFragment.java         # Edit emotion + description of an entry
│   │   └── EditEntryViewModel.java        # Validates + updates entry via JournalRepository
│   │
│   ├── hope/
│   │   ├── HopeFragment.java              # Image carousel with arrow navigation + heart icon
│   │   └── HopeViewModel.java             # Exposes image URLs from HopeRepository
│   │
│   ├── videos/
│   │   ├── VideosFragment.java            # RecyclerView of YouTube video cards
│   │   ├── VideoViewModel.java            # Exposes video list from VideoRepository
│   │   └── VideoAdapter.java              # Loads thumbnails via Glide; opens YouTube on tap
│   │
│   ├── hotline/
│   │   ├── HotlineFragment.java           # RecyclerView of crisis contact cards
│   │   ├── HotlineViewModel.java          # Exposes hotline list from HotlineRepository
│   │   └── HotlineAdapter.java            # Tap-to-call, email, Facebook link actions
│   │
│   └── profile/
│       ├── ProfileFragment.java           # Display name editing, read-only email + join date
│       └── ProfileViewModel.java          # loadProfile() + saveDisplayName() via ProfileRepository
│
├── data/
│   ├── SessionManager.java                # Firebase UID access, isLoggedIn(), clearSession()
│   │
│   ├── repository/
│   │   ├── AuthRepository.java            # signIn(), createUser(), sendPasswordReset()
│   │   ├── JournalRepository.java         # Room + Firestore operations│   │   │                                  
│   │   ├── HopeRepository.java            # Firebase Realtime DB listener for hope image URLs
│   │   ├── VideoRepository.java           # Firebase Realtime DB listener for video list
│   │   ├── HotlineRepository.java         # Firebase Realtime DB listener for hotline list
│   │   └── ProfileRepository.java         # Firestore read/write for user profile document;
│   │                                      #   createProfileIfAbsent(), updateDisplayName()
│   │
│   └── local/
│       ├── AppDatabase.java               # Room singleton (v12); entities: Journal, Video, Hotline
│       ├── AppExecutors.java              # Background thread pool helper
│       ├── dao/
│       │   ├── JournalEntryDao.java       # CRUD + findUnsyncedEntries(), findByFirestoreId()
│       │   ├── VideoDao.java
│       │   └── HotlineDao.java
│       └── entity/
│           ├── JournalEntryEntity.java    # entryId, userId, emotion, description,createdAtEpochMs
│           ├── VideoEntity.java           # videoId, title, thumbnailUrl, order
│           ├── HotlineEntity.java         # name, phone, email, facebookUrl, order
│           └── HotlineEntry.java          # PODO used during Realtime DB deserialization
│
└── util/
    ├── SyncJournalWorker.java             # WorkManager worker — pushes unsynced entries to Firestore
    └── NetworkUtils.java                  # isConnected() — checks WiFi/cellular/ethernet
```
## Firebase Data Structure

```
Firestore
└── users/
    └── {userId}/              { displayName, email, createdAt }

└── journal_entries/
    └── {userId}/
        └── entries/
            └── {firestoreId}/ { emotion, description, createdAtEpochMs, firestoreId }


Realtime Database
├── hope_images/
│   └── image_1/  { url, order }
├── videos/
│   └── vid_1/    { videoId, title, order }
└── hotlines/
    └── entry_1/  { name, phone, email, facebookUrl, order }

```

> **Note:** Hope screen images stored in Firebase Storage while video URL are store in Realtime Database

---

## Setup & Run
Choose the option that fits your goal — **Option 1** if you want to build and run the source code, **Option 2** if you just want to install and try the app.

--- 

### Option 1 — Build from Source (GitHub)

#### Prerequisites

- Android Studio (latest stable)
- Android SDK (API 26+)

> **No Firebase setup required.** The project uses the team's Firebase. The `google-services.json` is included in the repository. The only extra step is registering your machine's debug SHA-1 fingerprint so that **Google Sign-In** works on your build.

#### Steps

1. Clone the repository
   ```bash
   git clone https://github.com/Laizeu/Mobile-Development-Application-MindJar.v2.git
   ```
2. Open the project in Android Studio and let Gradle sync finish

3. Connect an Android device or start an emulator (API 26+)
4. Click **Run ▶️**

> **Optional — Enable Google Sign-In**
>
> Email/password login works out of the box. If you also want to use Google Sign-In, your debug SHA-1 fingerprint needs to be registered with the team's Firebase project.
>
> **Get your SHA-1:** In Android Studio, open the **Terminal** tab and run:
> ```bash
> # macOS / Linux
> ./gradlew signingReport
>
> # Windows
> gradlew signingReport
> ```
> Look for the `debug` variant block and copy the **SHA1** value, e.g.:
> ```
> SHA1: A1:B2:C3:D4:E5:F6:...
> ```
> Then share it with a team member who has Firebase Console access. They will add it under:
> > Firebase Console → Project Settings → Your apps → Android app → Add fingerprint

---

### Option 2 — Install via APK

Use this option to install MindJar directly on an Android device without building from source.

#### Requirements

- Android device running **Android 8.0 (API 26) or higher**
- "Install unknown apps" permission enabled on your device

#### Steps

1. Download the latest `mindjar-release.apk` from the [Releases](https://github.com/Laizeu/Mobile-Development-Application-MindJar.v2/releases) page
2. Transfer the APK to your Android device (via USB, Google Drive, or email)
3. On your device, open the APK file
4. If prompted, allow installation from unknown sources:
   - Go to **Settings → Apps → Special app access → Install unknown apps**
   - Enable it for the app you're using to open the APK (e.g. Files, Chrome)
5. Tap **Install** and wait for the installation to complete
6. Open **MindJar** from your app drawer

---

## Team — MO-IT119 Group G (Mapua-Malayan Digital College)

| Name | 
|---|
| John Paul Arquita |
| Christian John Batuigas |
| Danilo Giltendez |
| Laiza Veronica Llanto |
| Kenneth Ian Lu |

