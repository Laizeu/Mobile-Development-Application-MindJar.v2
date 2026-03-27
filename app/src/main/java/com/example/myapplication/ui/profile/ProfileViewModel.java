package com.example.myapplication.ui.profile;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.myapplication.data.repository.ProfileRepository;

public class ProfileViewModel extends AndroidViewModel {

    private final ProfileRepository repository = new ProfileRepository();

    private final MutableLiveData<String> displayName   = new MutableLiveData<>();
    private final MutableLiveData<String> email         = new MutableLiveData<>();
    private final MutableLiveData<Long>   joinedAt      = new MutableLiveData<>();
    private final MutableLiveData<String> statusMessage = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application app) { super(app); }

    public LiveData<String> getDisplayName()   { return displayName;   }
    public LiveData<String> getEmail()         { return email;         }
    public LiveData<Long>   getJoinedAt()      { return joinedAt;      }
    public LiveData<String> getStatusMessage() { return statusMessage; }

    public void loadProfile() {
        repository.loadProfile(new ProfileRepository.LoadCallback() {
            @Override
            public void onLoaded(String name, String mail, long joined) {
                displayName.postValue(name);
                email.postValue(mail);
                joinedAt.postValue(joined);
            }
            @Override
            public void onError(String message) {
                statusMessage.postValue("Could not load profile: " + message);
            }
        });
    }

    public void saveDisplayName(String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            statusMessage.setValue("Display name cannot be empty.");
            return;
        }
        repository.updateDisplayName(newName.trim(),
                new ProfileRepository.ProfileCallback() {
                    @Override public void onSuccess() {
                        displayName.postValue(newName.trim());
                        statusMessage.postValue("Profile updated!");
                    }
                    @Override public void onError(String message) {
                        statusMessage.postValue("Failed to save: " + message);
                    }
                });
    }

    // Call this from the Fragment after showing a Toast so the message
    // does not re-fire after a screen rotation.
    public void clearStatus() {
        statusMessage.setValue(null);
    }
}
