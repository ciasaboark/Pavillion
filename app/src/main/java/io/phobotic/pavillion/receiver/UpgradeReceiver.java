package io.phobotic.pavillion.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import io.phobotic.pavillion.BuildConfig;
import io.phobotic.pavillion.activity.MainActivity;
import io.phobotic.pavillion.schedule.EmailScheduler;

/**
 * Created by Jonathan Nelson on 8/15/16.
 */

public class UpgradeReceiver extends BroadcastReceiver {
    private static final String TAG = UpgradeReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) {
            Log.d(TAG, "Received package upgraded broadcast");

            //reschedule the email service
            EmailScheduler scheduler = new EmailScheduler(context);
            scheduler.reschedule();

            //restart the main activity
            if (BuildConfig.DEBUG == true) {
                Log.d(TAG, "Skipping restarting main activity after upgrade in debug release");
            } else {
                Intent i = new Intent(context, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        }
    }
}
