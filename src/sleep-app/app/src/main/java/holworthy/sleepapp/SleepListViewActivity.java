package holworthy.sleepapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;

public class SleepListViewActivity extends AppCompatActivity {
    private ListView fileListView;
    private ArrayList<SleepFile> sleepFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_list_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sleepFiles = new ArrayList<>();
        for (File file : MainActivity.getSleepFiles())
            sleepFiles.add(new SleepFile(file));

        fileListView = findViewById(R.id.fileListView);
        TextView emptyTextView = findViewById(R.id.empty);
        fileListView.setEmptyView(emptyTextView);

        ArrayAdapter<SleepFile> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sleepFiles);
        fileListView.setAdapter(adapter);
        fileListView.setOnItemClickListener((adapterView, view, position, id) -> {
            SleepFile sleepFile = (SleepFile) adapterView.getItemAtPosition(position);
            Intent intent = new Intent(SleepListViewActivity.this, AnalysisActivity.class);
            intent.putExtra("file", sleepFile.getFile());
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return true;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}