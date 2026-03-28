package com.example.myapplication.ui.profile;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.myapplication.data.repository.ProfileRepository;

import com.example.myapplication.data.local.AppDatabase;

public class ProfileViewModel extends AndroidViewModel {

    private final ProfileRepository repository = new ProfileRepository();

    private final MutableLiveData<String>  displayName   = new MutableLiveData<>();
    private final MutableLiveData<String>  email         = new MutableLiveData<>();
    private final MutableLiveData<Long>    joinedAt      = new MutableLiveData<>();
    private final MutableLiveData<String>  statusMessage = new MutableLiveData<>();
    private final MutableLiveData<Integer> avatarId      = new MutableLiveData<>(1);


    public ProfileViewModel(@NonNull Application app) { super(app); }

    public LiveData<String> getDisplayName()   { return displayName;   }
    public LiveData<String> getEmail()         { return email;         }
    public LiveData<Long>   getJoinedAt()      { return joinedAt;      }
    public LiveData<String> getStatusMessage() { return statusMessage; }

    public void loadProfile() {
        repository.loadProfile(new ProfileRepository.LoadCallback() {
            @Override
            public void onLoaded(String name, String mail, long joined, int id) {
                displayName.postValue(name);
                email.postValue(mail);
                joinedAt.postValue(joined);
                avatarId.postValue(id);
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

    public LiveData<Integer> getAvatarId() { return avatarId; }

    public void saveAvatarId(int id) {
        avatarId.setValue(id);   // update UI instantly (optimistic)
        repository.saveAvatarId(id, new ProfileRepository.ProfileCallback() {
            @Override public void onSuccess() {
                statusMessage.postValue("Avatar updated!");
            }
            @Override public void onError(String message) {
                statusMessage.postValue("Failed to save avatar: " + message);
            }
        });
    }


    public void deleteAccount(android.content.Context context, AppDatabase roomDb) {
        repository.deleteAccount(context, roomDb,
                new ProfileRepository.DeleteCallback() {
                    @Override public void onSuccess() {
                        statusMessage.postValue("ACCOUNT_DELETED");
                    }
                    @Override public void onError(String message) {
                        statusMessage.postValue("Error: " + message);
                    }
                });
    }



}
