package io.phobotic.pavillion.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import io.phobotic.pavillion.schedule.EmailScheduler;

/**
 * Created by Jonathan Nelson on 6/3/16.
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = BootReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            EmailScheduler scheduler = new EmailScheduler(context);
            scheduler.reschedule();
        }
    }
}
