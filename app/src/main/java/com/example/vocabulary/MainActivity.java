package com.example.vocabulary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    EditText inputword;
    TextView sim_word;
    String word;
    ArrayList<Word> voc_words;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputword = findViewById(R.id.input);
        sim_word = findViewById(R.id.answer);

        inputword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    inputword.setText("");
                    sim_word.setText("");
                }
            }
        });
        initWordDatabase();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initWordDatabase();
    }


    public void initWordDatabase(){
        WordList req = new WordList();
        req.execute();
    }

    class WordList extends AsyncTask<Void, List<Word>, List<Word>> {
        @Override
        protected List<Word> doInBackground(Void... voids) {
            WordsDatabase db = WordsDatabase.getDatabase(MainActivity.this);
            return db.wordsDao().getAll();
        }

        @Override
        protected void onPostExecute(List<Word> db) {
            super.onPostExecute(db);
            voc_words = new ArrayList<>(db);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.listWords:
                showDictionary();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showDictionary() {
        ArrayList<String> dict = new ArrayList<>();
        for (Word word: voc_words){
            dict.add(word.word);
        }
        Intent intent = new Intent(this, ListWordsActivity.class);
        intent.putStringArrayListExtra("listWord", dict);
        startActivity(intent);
    }


    public int DamerauLevenshtein(String a, String b) {
        int[][] dist = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i < a.length() + 1; i++) {
            dist[i][0] = i;
        }
        for (int j = 0; j < b.length() + 1; j++) {
            dist[0][j] = j;
        }
        for (int i = 1; i < a.length() + 1; i++) {
            for (int j = 1; j < b.length() + 1; j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                int insert = dist[i][j - 1] + 1;
                int delete = dist[i - 1][j] + 1;
                int change = dist[i - 1][j - 1] + cost;
                dist[i][j] = Math.min(Math.min(insert, delete), change);
                if (i > 1 && j > 1 && a.charAt(i - 1) == b.charAt(j - 2) && a.charAt(i - 2) == b.charAt(j - 1)) {
                    dist[i][j] = Math.min(dist[i][j], dist[i - 2][j - 2] + cost);
                }
            }
        }
        return dist[a.length()][b.length()];
    }


    public void checkWord(View view) {
        inputword.clearFocus();
        word = inputword.getText().toString();
        sim_word.setTextColor(Color.BLACK);
        String most_similar = "";
        int limit = (int) Math.ceil(0.4 * word.length());

        if (voc_words.size() != 0) {
            if (!word.equals("")) {
                int minDamLev = DamerauLevenshtein(word.toLowerCase(), voc_words.get(0).word.toLowerCase());
                most_similar = voc_words.get(0).word;
                if (minDamLev == 0) {
                    sim_word.setTextColor(Color.GREEN);
                    sim_word.setText(most_similar);
                } else {
                    for (int i = 1; i < voc_words.size(); i++) {
                        if (DamerauLevenshtein(word.toLowerCase(), voc_words.get(i).word.toLowerCase()) == 0) {
                            minDamLev = 0;
                            most_similar = voc_words.get(i).word;
                            break;
                        }
                        if (DamerauLevenshtein(word.toLowerCase(), voc_words.get(i).word.toLowerCase()) < minDamLev) {
                            most_similar = voc_words.get(i).word;
                            minDamLev = DamerauLevenshtein(word.toLowerCase(), voc_words.get(i).word.toLowerCase());
                        }
                    }
                    if ((minDamLev < limit)) {
                        if (word.toLowerCase().equals(most_similar.toLowerCase().replace('ё','е'))){
                            sim_word.setTextColor(Color.GREEN);
                        }
                        sim_word.setText(most_similar);
                    } else {
                        sim_word.setText("нет в словаре");
                    }
                }
            }
        }else Toast.makeText(getBaseContext(),"Не задан словарь",Toast.LENGTH_LONG).show();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        WordsDatabase.wordsDB.cleanUp();
    }
}