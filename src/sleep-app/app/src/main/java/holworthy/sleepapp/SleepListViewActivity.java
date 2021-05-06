package holworthy.sleepapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SleepListViewActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_list_view);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ArrayList<File> sleepFiles = new ArrayList<>(Arrays.asList(Utils.getSleepAnalysisFiles()));

        ListView fileListView = findViewById(R.id.fileListView);
        TextView emptyTextView = findViewById(R.id.empty);
        fileListView.setEmptyView(emptyTextView);

        class MyArrayAdapter extends ArrayAdapter<File> {
            private ArrayList<File> sleepFiles;

            public MyArrayAdapter(@NonNull Context context, @NonNull List<File> sleepFiles) {
                super(context, -1, sleepFiles);
                this.sleepFiles = (ArrayList<File>) sleepFiles;
            }

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                File sleepFile = sleepFiles.get(position);
                LinearLayout linearLayout = new LinearLayout(SleepListViewActivity.this);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                linearLayout.setPadding(30, 30, 30, 30);

                TextView titleTextView = new TextView(SleepListViewActivity.this);
                titleTextView.setTextColor(0xffffffff);
                titleTextView.setText(sleepFile.toString());
                titleTextView.setTypeface(Typeface.DEFAULT_BOLD);
                linearLayout.addView(titleTextView);
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Thread titleThread = new Thread(() -> {
                    try {
                        long startTimestamp = Utils.getSleepAnalysisFileStartTimestamp(sleepFile);
                        titleTextView.post(() -> titleTextView.setText(simpleDateFormat.format(startTimestamp)));
                    } catch (Exception e) {

                    }
                });
                titleThread.start();

                RelativeLayout infoLayout = new RelativeLayout(SleepListViewActivity.this);
                linearLayout.addView(infoLayout);

                TextView durationTextView = new TextView(SleepListViewActivity.this);
                durationTextView.setTextColor(0xffffffff);
                Thread thread = new Thread(() -> {
                    long duration;
                    try {
                        duration = Utils.getSleepAnalysisFileDuration(sleepFile);
                    } catch (Exception e) {
                        duration = 0;
                    }
                    String message = Utils.timeStringFromDuration(duration);
                    durationTextView.post(() -> durationTextView.setText(message));
                });
                thread.start();
                durationTextView.setText("Loading...");
                infoLayout.addView(durationTextView);

                Button removeButton = new Button(SleepListViewActivity.this);
                removeButton.setText("Delete sleep");
                removeButton.setTextColor(0xffffffff);
                removeButton.setBackgroundTintList(SleepListViewActivity.this.getResources().getColorStateList(R.color.buttonred));
                removeButton.setOnClickListener(v -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SleepListViewActivity.this);
                    builder.setCancelable(true);
                    builder.setTitle("Delete This Sleep?");
                    builder.setMessage("Are you sure you want to delete, THIS CANNOT BE UNDONE.");
                    builder.setPositiveButton("Confirm", (dialog, which) -> {
                        if(sleepFiles.get(position).delete()) {
                            sleepFiles.remove(position);
                            MyArrayAdapter.this.notifyDataSetChanged();
                        }
                    });
                    builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {});
                    AlertDialog dialog = builder.create();
                    dialog.show();
                });
                removeButton.setFocusable(false);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.ALIGN_PARENT_END);
                removeButton.setLayoutParams(params);
                infoLayout.addView(removeButton);

                return linearLayout;
            }
        }

        MyArrayAdapter adapter = new MyArrayAdapter(this, sleepFiles);
        fileListView.setAdapter(adapter);
        fileListView.setOnItemClickListener((adapterView, view, position, id) -> {
            File sleepFile = (File) adapterView.getItemAtPosition(position);
            Intent intent = new Intent(SleepListViewActivity.this, AnalysisActivity.class);
            intent.putExtra("file", sleepFile);
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
