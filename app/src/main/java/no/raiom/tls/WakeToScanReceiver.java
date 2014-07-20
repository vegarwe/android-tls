package no.raiom.tls;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class WakeToScanReceiver extends WakefulBroadcastReceiver {
    private static AlarmManager alarmMgr;
    private static PendingIntent alarmIntent;
    private static boolean start_scanning = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Fisken", "WakeToScanReceiver.onReceive; " + start_scanning);

        if (start_scanning) {
            _setAlarm(context, 1000 *  3);
        } else {
            _setAlarm(context, 1000 * 57);
        }

        Intent service = new Intent(context, BleScanService.class);
        service.putExtra("start_scanning", start_scanning);
        startWakefulService(context, service);

        start_scanning = !start_scanning;
    }

    public void setAlarm(Context context) {
        Log.i("Fisken", "WakeToScanReceiver.setAlarm");

        _setAlarm(context, 0);

//        ComponentName receiver = new ComponentName(context, BootReceiver.class);
//        PackageManager pm = context.getPackageManager();
//
//        pm.setComponentEnabledSetting(receiver,
//                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
//                PackageManager.DONT_KILL_APP);
    }

    public void cancelAlarm(Context context) {
        Log.e("Fisken", "WakeToScanReceiver.cancelAlarm");

        _initAlarmMgr(context);
        alarmMgr.cancel(alarmIntent);

//        ComponentName receiver = new ComponentName(context, BootReceiver.class);
//        PackageManager pm = context.getPackageManager();
//
//        pm.setComponentEnabledSetting(receiver,
//                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
//                PackageManager.DONT_KILL_APP);
    }

    private void _setAlarm(Context context, int millis) {
        _initAlarmMgr(context);
        alarmMgr.setExact(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + (millis ), alarmIntent);
    }

    private void _initAlarmMgr(Context context) {
        if (alarmMgr == null) {
            alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, WakeToScanReceiver.class);
            alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        }
    }
}
