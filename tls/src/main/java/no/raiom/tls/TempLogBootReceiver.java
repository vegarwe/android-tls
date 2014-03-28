package no.raiom.tls;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class TempLogBootReceiver extends BroadcastReceiver {
    TempLogWakeToScanReceiver alarm = new TempLogWakeToScanReceiver();
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            alarm.setAlarm(context);
        }
    }
}
