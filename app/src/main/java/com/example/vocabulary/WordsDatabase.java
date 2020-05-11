package com.example.vocabulary;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.ArrayList;
import java.util.concurrent.Executors;


@Database(entities = {Word.class}, version = 1)
public abstract class WordsDatabase extends RoomDatabase {
    public abstract WordsDao wordsDao();
    public static volatile WordsDatabase wordsDB;

    public synchronized static WordsDatabase getDatabase(Context context) {
        if (wordsDB == null) {
            wordsDB = buildDatabase(context);
        }
        return wordsDB;
    }

    public static WordsDatabase buildDatabase(final Context context) {
        return Room.databaseBuilder(context.getApplicationContext(), WordsDatabase.class, "words")
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        Executors.newSingleThreadScheduledExecutor().execute(new Runnable() {
                            @Override
                            public void run() {
                                ArrayList<Word> words = new ArrayList();
                                String[] start_words = context.getResources().getStringArray(R.array.voc_words);
                                for (String w : start_words) {
                                    words.add(new Word(w));
                                }
                                getDatabase(context.getApplicationContext()).wordsDao().insertAll(words);
                            }
                        });
                    }
                })
                .fallbackToDestructiveMigration()
                .build();
    }

    public void cleanUp() {
        wordsDB.close();
        wordsDB = null;
    }
}

