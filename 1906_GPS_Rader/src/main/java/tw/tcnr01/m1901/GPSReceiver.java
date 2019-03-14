package tw.tcnr01.m1901;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Vibrator;
import android.widget.Toast;

public class GPSReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "已經接近景點範圍",
                Toast.LENGTH_LONG).show();
        Vibrator vibrator = (Vibrator) context.getSystemService(
                Context.VIBRATOR_SERVICE);
        vibrator.vibrate(500); // 0.5秒
        ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        toneG.startTone(ToneGenerator.TONE_CDMA_INTERCEPT,500);
        toneG.stopTone();
    }
}

