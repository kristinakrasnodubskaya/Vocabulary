package com.example.vocabulary;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface WordsDao {
    @Query("SELECT * FROM words ORDER BY LOWER(word) ASC")
    List<Word> getAll();

    @Query("SELECT * FROM words WHERE word = :word")
    Word getByName(String word);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Word word);

    @Insert
    void insertAll(ArrayList<Word> words);

    @Delete
    void delete(Word word);
}
