package holworthy.sleepapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

        class MyArrayAdapter extends ArrayAdapter<SleepFile> {
            private ArrayList<SleepFile> sleepFiles;

            public MyArrayAdapter(@NonNull Context context, @NonNull List<SleepFile> sleepFiles) {
                super(context, -1, sleepFiles);
                this.sleepFiles = (ArrayList<SleepFile>) sleepFiles;
            }

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                SleepFile sleepFile = sleepFiles.get(position);
                LinearLayout linearLayout = new LinearLayout(SleepListViewActivity.this);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                linearLayout.setPadding(30, 30, 30, 30);

                TextView titleTextView = new TextView(SleepListViewActivity.this);
                titleTextView.setText(sleepFile.toString());
                titleTextView.setTypeface(Typeface.DEFAULT_BOLD);
                linearLayout.addView(titleTextView);

                LinearLayout infoLayout = new LinearLayout(SleepListViewActivity.this);
                infoLayout.setOrientation(LinearLayout.HORIZONTAL);
                linearLayout.addView(infoLayout);

                TextView durationTextView = new TextView(SleepListViewActivity.this);
                Thread thread = new Thread(() -> {
                    long duration;
                    try {
                        duration = MainActivity.getSleepFileLength(sleepFile.getFile());
                    } catch (Exception e) {
                        duration = 0;
                    }
                    long hours = duration / (1000 * 60 * 60);
                    long minutes = (duration / (1000 * 60)) % 60;
                    String message = hours == 0 && minutes == 0 ? "0 minutes" : hours == 0 ? minutes + " minute" + (minutes == 1 ? "" : "s") : minutes == 0 ? hours + " hour" + (hours == 1 ? "" : "s") : hours + " hour" + (hours == 1 ? "" : "s") + " " + minutes + " minute" + (minutes == 1 ? "" : "s");
                    durationTextView.post(() -> durationTextView.setText(message));
                });
                thread.start();
                durationTextView.setText("Loading...");
                infoLayout.addView(durationTextView);

                Button removeButton = new Button(SleepListViewActivity.this);
                removeButton.setText("Delete sleep");
                removeButton.setBackgroundTintList(SleepListViewActivity.this.getResources().getColorStateList(R.color.buttonred));
                removeButton.setOnClickListener(v -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SleepListViewActivity.this);
                    builder.setCancelable(true);
                    builder.setTitle("Delete This Sleep?");
                    builder.setMessage("Are you sure you want to delete, THIS CANNOT BE UNDONE.");
                    builder.setPositiveButton("Confirm",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (sleepFiles.get(position).getFile().delete()){
                                        sleepFiles.remove(position);
                                        MyArrayAdapter.this.notifyDataSetChanged();
                                    }
                                }
                            });
                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();


                });
                removeButton.setFocusable(false);

                infoLayout.addView(removeButton);

                return linearLayout;
            }
        }

        MyArrayAdapter adapter = new MyArrayAdapter(this, sleepFiles);
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