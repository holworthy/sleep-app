package holworthy.sleepapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.PowerManager;

public class MainActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

//		PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
//		PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "sleepapp:lock");
//		wakeLock.acquire();
//		wakeLock.release();
	}
}
