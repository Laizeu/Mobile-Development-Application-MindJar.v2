package com.example.myapplication.data.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import androidx.work.WorkManager;
import com.example.myapplication.data.local.AppDatabase;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.example.myapplication.R;

import com.google.firebase.functions.FirebaseFunctions;

public class ProfileRepository {

    private final FirebaseFirestore db   = FirebaseFirestore.getInstance();
    private final FirebaseAuth       auth = FirebaseAuth.getInstance();

    // ── Callback interfaces ────────────────────────────────────────
    public interface ProfileCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface LoadCallback {
        void onLoaded(String displayName, String email, long joinedAt, int avatarId);
        void onError(String message);
    }
    public void loadProfile(LoadCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) { callback.onError("Not logged in"); return; }

        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    String name   = doc.getString("displayName");
                    String email  = doc.getString("email");
                    Long   joined = doc.getLong("joinedAtEpochMs");

                    // Fallback to FirebaseAuth when Firestore doc is absent
                    if (name  == null) name  = user.getDisplayName();
                    if (email == null) email = user.getEmail();
                    if (joined == null) {
                        joined = (user.getMetadata() != null)
                                ? user.getMetadata().getCreationTimestamp()
                                : System.currentTimeMillis();
                    }

                    Long avatarIdLong = doc.getLong("avatarId");
                    int  avatarId     = (avatarIdLong != null) ? avatarIdLong.intValue() : 1;

                    callback.onLoaded(
                            name  != null ? name  : "",
                            email != null ? email : "",
                            joined,
                            avatarId
                    );

                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // ── updateDisplayName() ─────────────────────────────────────────
    // Step 1 — Update Firestore (source of truth for profile reads).
    // Step 2 — Update FirebaseAuth display name so the welcome greeting
    //          on the Home screen also refreshes.
    // Both steps must succeed; if Firestore fails we do not update Auth.
    public void updateDisplayName(String newName, ProfileCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) { callback.onError("Not logged in"); return; }

        Map<String, Object> update = new HashMap<>();
        update.put("displayName", newName);

        db.collection("users").document(user.getUid())
                .set(update, SetOptions.merge())          // merge = keep other fields
                .addOnSuccessListener(unused -> {
                    UserProfileChangeRequest req =
                            new UserProfileChangeRequest.Builder()
                                    .setDisplayName(newName).build();
                    user.updateProfile(req)
                            .addOnSuccessListener(v -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void createProfileIfAbsent() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        DocumentReference ref =
                db.collection("users").document(user.getUid());

        ref.get().addOnSuccessListener(doc -> {
            if (!doc.exists()) {
                long joined = (user.getMetadata() != null)
                        ? user.getMetadata().getCreationTimestamp()
                        : System.currentTimeMillis();

                Map<String, Object> data = new HashMap<>();
                data.put("displayName",
                        user.getDisplayName() != null ? user.getDisplayName() : "");
                data.put("email",
                        user.getEmail() != null ? user.getEmail() : "");
                data.put("joinedAtEpochMs", joined);
                ref.set(data);
            }
        });
    }

    public void saveAvatarId(int avatarId, ProfileCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) { callback.onError("Not logged in"); return; }

        Map<String, Object> update = new HashMap<>();
        update.put("avatarId", avatarId);

        db.collection("users").document(user.getUid())
                .set(update, SetOptions.merge())
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }


    public interface DeleteCallback {
        void onSuccess();
        void onError(String message);
    }

    public void deleteAccount(Context context, AppDatabase roomDb, DeleteCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onError("Session expired. Please log in again.");
            return;
        }

        String uid = user.getUid();

        // Step 1 — Cancel WorkManager sync jobs
        WorkManager.getInstance(context).cancelAllWorkByTag("sync_journal");

        // Step 2 — Delete Firestore profile document
        db.collection("users").document(uid)
                .delete()
                .addOnSuccessListener(unused1 -> {

                    // Step 3 — Fetch and batch-delete Firestore journal entries
                    db.collection("journal_entries").document(uid)
                            .collection("entries")
                            .get()
                            .addOnSuccessListener(snapshot -> {
                                WriteBatch batch = db.batch();
                                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                                    batch.delete(doc.getReference());
                                }

                                batch.commit().addOnSuccessListener(unused2 -> {

                                    // Step 4 — Delete Room on background thread
                                    new Thread(() -> {
                                        roomDb.journalEntryDao().deleteAllByUser(uid);

                                        // Step 5 — Return to MAIN thread before calling
                                        // Firebase Functions (requires main thread context)
                                        new android.os.Handler(
                                                android.os.Looper.getMainLooper()).post(() -> {

                                            // Step 6 — Force refresh token then call function
                                            FirebaseAuth.getInstance()
                                                    .getCurrentUser()
                                                    .getIdToken(true)
                                                    .addOnSuccessListener(tokenResult -> {

                                                        FirebaseFunctions functions = FirebaseFunctions.getInstance("us-central1");
                                                        functions.getHttpsCallable("deleteAccount")
                                                                .call()
                                                                .addOnSuccessListener(result -> {

                                                                    // Step 7 — Sign out Firebase
                                                                    auth.signOut();

                                                                    // Step 8 — Sign out Google
                                                                    GoogleSignInOptions gso =
                                                                            new GoogleSignInOptions.Builder(
                                                                                    GoogleSignInOptions.DEFAULT_SIGN_IN)
                                                                                    .requestIdToken(context.getString(
                                                                                            R.string.default_web_client_id))
                                                                                    .requestEmail()
                                                                                    .build();

                                                                    GoogleSignIn.getClient(context, gso)
                                                                            .signOut()
                                                                            .addOnCompleteListener(t ->
                                                                                    callback.onSuccess());
                                                                })
                                                                .addOnFailureListener(e ->
                                                                        callback.onError("Auth delete failed: "
                                                                                + e.getMessage()));
                                                    })
                                                    .addOnFailureListener(e ->
                                                            callback.onError("Token refresh failed: "
                                                                    + e.getMessage()));
                                        });

                                    }).start();

                                }).addOnFailureListener(e ->
                                        callback.onError("Journal delete failed: " + e.getMessage()));
                            })
                            .addOnFailureListener(e ->
                                    callback.onError("Could not fetch journals: " + e.getMessage()));
                })
                .addOnFailureListener(e ->
                        callback.onError("Profile delete failed: " + e.getMessage()));
    }
}
