package io.phobotic.pavillion.receiver;

import android.content.Context;
import android.content.Intent;

import org.junit.Test;

/**
 * Created by Jonathan Nelson on 8/25/16.
 */
public class UpgradeReceiverTest {

    //after the broadcast is received we expect an alarm to be set
    @Test
    public void testOnReceive() throws Exception {

    }

    private class TestableUpgradeReceiver extends UpgradeReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);
        }
    }
}