package no.raiom.tls;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class BleScanService extends Service {
    Map<String, Boolean> addrs;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        addrs = new HashMap<String, Boolean>();
        if (intent.hasExtra("device_addrs")) {
            for (String device_addr : intent.getStringArrayListExtra("device_addrs")) {
                addrs.put(device_addr, false);
                Log.i("Fisken", "device_addr: " + device_addr);
            }
        }

        scanLeDevice(intent.getBooleanExtra("start_scanning", false));
        WakeToScanReceiver.completeWakefulIntent(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e("Fisken", "BleScanService.onBind");
        return null;
    }

    private void scanLeDevice(final boolean enable) {
        final BluetoothAdapter mBluetoothAdapter =
                ((BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();

        if (enable) {
            mBluetoothAdapter.startLeScan(leScanCallback);
            Log.i("Fisken", "BleScanService.startLeScan");
        } else {
            Log.i("Fisken", "BleScanService.stopLeScan");
            mBluetoothAdapter.stopLeScan(leScanCallback);
        }
    }

    private BluetoothAdapter.LeScanCallback leScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    Log.d("Fisken", "Found device: " + device.getName() + " " + device.getAddress());
                    if (addrs.containsKey(device.getAddress())) {
                        if (! addrs.get(device.getAddress())) {
                            addrs.put(device.getAddress(), true);

                            Log.d("Fisken", "Starting service for: " + device.getAddress());
                            Intent service = new Intent(getBaseContext(), TempLogDeviceService.class);
                            service.putExtra("device_addr", device.getAddress());
                            startService(service);
                        }
                    }
                }
            };
}
