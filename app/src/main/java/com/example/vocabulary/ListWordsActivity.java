package com.example.vocabulary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.concurrent.Executors;

public class ListWordsActivity extends AppCompatActivity {
    ListView listView;
    ArrayList<String> dict = new ArrayList<>();
    String newWord;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_words);

        listView = findViewById(R.id.listWords);

        dict = getIntent().getStringArrayListExtra("listWord");
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dict);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ListWordsActivity.this);
                builder.setMessage(dict.get(position));
                builder.setNegativeButton("удалить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final Word wd = new Word(dict.get(position));
                        dict.remove(position);
                        adapter = new ArrayAdapter<>(ListWordsActivity.this, android.R.layout.simple_list_item_1, dict);
                        listView.setAdapter(adapter);
                        Executors.newSingleThreadScheduledExecutor().execute(new Runnable() {
                            @Override
                            public void run() {
                                WordsDatabase db = WordsDatabase.getDatabase(ListWordsActivity.this);
                                db.wordsDao().delete(wd);
                            }
                        });
                    }
                });
                builder.show();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                addWord();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void addWord() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_word, null);
        builder.setView(view);
        final EditText edt = view.findViewById(R.id.newWordd);
        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                newWord = edt.getText().toString();
                if (!newWord.equals("")) {
                    AddWord req = new AddWord();
                    req.execute(newWord);
                }
            }
        });
        builder.show();
    }


    class AddWord extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... words) {
            WordsDatabase db = WordsDatabase.getDatabase(ListWordsActivity.this);
            Word word_in_db = db.wordsDao().getByName(words[0]);
            if (word_in_db == null) {
                db.wordsDao().insert(new Word(words[0]));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            boolean unique = true;
            for (String d : dict) {
                if (newWord.equals(d)) {
                    unique = false;
                    break;
                }
            }
            if (unique) {
                dict.add(0, newWord);
                adapter.notifyDataSetChanged();
            }
        }
    }
}
