package no.raiom.tls;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class BleScanService extends Service {
    public static final String TAG = BleScanService.class.getSimpleName();

    private BluetoothAdapter mBluetoothAdapter;
    private TempLogDeviceAction mDeviceAction = new TempLogDeviceAction(this);

    public BleScanService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        scanLeDevice(intent.getBooleanExtra("start_scanning", false));
        TempLogWakeToScanReceiver.completeWakefulIntent(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e("Fisken", "BleScanService.onBind");
        return null;
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mBluetoothAdapter.startLeScan(mDeviceAction.leScanCallback);
            Log.i("Fisken", "BleScanService.startLeScan");
        } else {
            Log.i("Fisken", "BleScanService.stopLeScan");
            mBluetoothAdapter.stopLeScan(mDeviceAction.leScanCallback);
        }
    }
}
