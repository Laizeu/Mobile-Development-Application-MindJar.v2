package com.example.myapplication.data.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.HashMap;
import java.util.Map;

public class ProfileRepository {

    private final FirebaseFirestore db   = FirebaseFirestore.getInstance();
    private final FirebaseAuth       auth = FirebaseAuth.getInstance();

    // ── Callback interfaces ────────────────────────────────────────
    public interface ProfileCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface LoadCallback {
        void onLoaded(String displayName, String email, long joinedAt);
        void onError(String message);
    }

    // ── loadProfile() ──────────────────────────────────────────────
    // Reads Firestore first. Falls back to FirebaseAuth values if the
    // document does not exist yet (e.g. accounts created before this
    // feature was added, or a Google user who never triggered sign-up).
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

                    callback.onLoaded(
                            name  != null ? name  : "",
                            email != null ? email : "",
                            joined
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

    // ── createProfileIfAbsent() ─────────────────────────────────────
    // Writes the initial Firestore document for a new account.
    // Safe to call on every sign-in — the doc.exists() check makes it
    // a no-op if the document already exists.
    // Call this from BOTH SignUpFragment (email) and LoginFragment (Google).
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
}
