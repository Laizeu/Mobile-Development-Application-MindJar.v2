package com.example.myapplication.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface JournalEntryDao {

    @Insert
    long insert(JournalEntryEntity entry);

    @Query("SELECT * FROM journal_entries WHERE userId = :userId ORDER BY createdAtEpochMs DESC")
    List<JournalEntryEntity> listForUser(long userId);

    @Query("SELECT * FROM journal_entries WHERE entryId = :entryId LIMIT 1")
    JournalEntryEntity findById(long entryId);

    @Update
    int update(JournalEntryEntity entry);
}
