package no.raiom.tls;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class BleScanService extends Service {
    private TempLogDeviceAction mDeviceAction;
    @Override
    public void onCreate() {
        super.onCreate();

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

    private boolean found_device;

    private void scanLeDevice(final boolean enable) {
        final BluetoothAdapter mBluetoothAdapter =
                ((BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();

        if (enable) {
            found_device = false;
            mBluetoothAdapter.startLeScan(leScanCallback);
            Log.i("Fisken", "BleScanService.startLeScan");
        } else {
            Log.i("Fisken", "BleScanService.stopLeScan");
            mBluetoothAdapter.stopLeScan(leScanCallback);
        }
    }

    private void connect(BluetoothDevice device) {
        if (mDeviceAction == null) {
            mDeviceAction = new TempLogDeviceAction();
        }

        //mDeviceAction.connect(this, device);
        device.connectGatt(this, false, mDeviceAction.leGattCallback);
    }

    private BluetoothAdapter.LeScanCallback leScanCallback =
            new BluetoothAdapter.LeScanCallback() {
            
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    //Log.e("Fisken", "Found device: " + device.getName() + " " + device.getAddress());
                    if (device.getName().equals("TLS_480204226")) {
                        if (!found_device) {
                            found_device = true;
                            connect(device);
                        }
                    }
                }
            };
}
