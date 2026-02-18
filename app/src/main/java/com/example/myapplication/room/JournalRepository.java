package com.example.myapplication.room;

import android.content.Context;
import java.util.List;

public class JournalRepository {

    private final JournalEntryDao dao;

    public JournalRepository(Context context) {
        dao = AppDatabase.getInstance(context).journalEntryDao();
    }

    public long addEntry(long userId, String emotion, String text) {
        JournalEntryEntity entry = new JournalEntryEntity(
                userId, emotion, text, false, System.currentTimeMillis()
        );
        return dao.insert(entry);
    }

    public List<JournalEntryEntity> listEntries(long userId) {
        return dao.listForUser(userId);
    }

    public JournalEntryEntity getEntry(long entryId) {
        return dao.findById(entryId);
    }
}
