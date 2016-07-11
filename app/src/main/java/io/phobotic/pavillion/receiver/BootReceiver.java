package io.phobotic.pavillion.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import io.phobotic.pavillion.service.SchedulerService;

/**
 * Created by Jonathan Nelson on 6/3/16.
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = BootReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent serviceLauncher = new Intent(context, SchedulerService.class);
            context.startService(serviceLauncher);
            Log.v(TAG, "Started scheduling service");
        }
    }
}
