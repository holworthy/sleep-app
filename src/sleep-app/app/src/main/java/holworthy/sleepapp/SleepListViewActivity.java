package holworthy.sleepapp;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

public class SleepListViewActivity extends AppCompatActivity {
    private ListView fileListView;
    private ArrayList<SleepFile> sleepFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_list_view);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        sleepFiles = new ArrayList<>();

        for (File file : MainActivity.getSleepFiles()){
            sleepFiles.add(new SleepFile(file));
        }

        fileListView = findViewById(R.id.fileListView);
        ArrayAdapter<SleepFile> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sleepFiles);
        fileListView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return true;
    }
}