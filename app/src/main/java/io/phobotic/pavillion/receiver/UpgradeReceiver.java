package io.phobotic.pavillion.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import io.phobotic.pavillion.schedule.EmailScheduler;

/**
 * Created by Jonathan Nelson on 8/15/16.
 */

public class UpgradeReceiver extends BroadcastReceiver {
    private static final String TAG = UpgradeReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) {
            EmailScheduler scheduler = new EmailScheduler(context);
            scheduler.reschedule();
        }
    }
}
