package com.example.myapplication.room;

import android.content.Context;

import org.mindrot.jbcrypt.BCrypt;

public class AuthRepository {

    private final UserDao userDao;

    public AuthRepository(Context context) {
        this.userDao = AppDatabase.getInstance(context).userDao();
    }

    public boolean emailExists(String email) {
        return userDao.countByEmail(email) > 0;
    }

    public long createUser(String fullName, String email, String rawPassword) {
        // BCrypt will generate salt internally when using gensalt()
        String hash = BCrypt.hashpw(rawPassword, BCrypt.gensalt(12));
        UserEntity user = new UserEntity(fullName, email, hash, System.currentTimeMillis());
        return userDao.insert(user);
    }

    public UserEntity getUserByEmail(String email) {
        return userDao.findByEmail(email);
    }

    public boolean verifyPassword(String rawPassword, String storedHash) {
        // storedHash includes salt + cost
        return BCrypt.checkpw(rawPassword, storedHash);
    }
}
