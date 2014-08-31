package no.raiom.tls;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TempLogScanner extends Service {
    public final static UUID TLS_SERVI = UUID.fromString("000018fa-0000-1000-8000-00805f9b34fb");

    private BluetoothLeScanner scanner;
    private boolean            keep_running;

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("Fisken", "TempLogScanner.onCreate");

        keep_running = false;
        registerReceiver(bluetoothStatusChangeReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("Fisken", "TempLogScanner.onStartCommand");

        // TODO: Check if bluetooth is enabled! Return if not

        keep_running = true;
        startScanner();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("Fisken", "TempLogScanner.onDestroy");

        keep_running = false;
        resetScanner();

		unregisterReceiver(bluetoothStatusChangeReceiver);
    }

    private final BroadcastReceiver bluetoothStatusChangeReceiver =
            new BroadcastReceiver() {

                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    Log.e("Fisken", "BluetoothStatusChange.onReceive " + action);
                    if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
                        if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) {
                            resetScanner();
                        } else if(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON){
                            startScanner();
                        }
                    }
                }
            };

    public void startScanner() {
        // Return if we should not keep running
        if (! keep_running) {
            Log.d("Fisken", "Start called with keep_running = false. This should not happen, do nothing");
            return;
        }

        if (scanner != null) {
            // TODO: Stop running scanner and create new one?
            Log.d("Fisken", "Scanner already running, do nothing");
            return;
        }

        // Get scanner object
        BluetoothManager manager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = manager.getAdapter();
        scanner                  = adapter.getBluetoothLeScanner();

        // Filters
        List<ScanFilter> filters = new ArrayList<ScanFilter>();
        ScanFilter.Builder filter_builder = new ScanFilter.Builder();
        filter_builder.setServiceUuid(new ParcelUuid(TLS_SERVI));
        filter_builder.setMacAddress("E9:E9:70:24:E7:80");
        filter_builder.setServiceData(ByteUtils.hexStringToByteArray("FE1801"));
        filters.add(filter_builder.build());

        // Settings
        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setCallbackType(ScanSettings.CALLBACK_TYPE_ON_UPDATE);
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
        ScanSettings settings = builder.build();

        // Start scan
        Log.i("Fisken", "TempLogScanner.startScanner: settings " + settings + " filters " + filters + " leScanCallback " + leScanCallback);
        scanner.startScan(filters, settings, leScanCallback);
    }

    public void stopScanner() {
        Log.i("Fisken", "TempLogScanner.stopScanner: " + leScanCallback);
        scanner.stopScan(leScanCallback);
    }

    private void resetScanner() {
        try {
            stopScanner();
        } catch (Exception e) {
            // Nothing really to do
        }
        scanner = null;
    }

    private ScanCallback leScanCallback =
            new ScanCallback() {

                @Override
                public void onAdvertisementUpdate(ScanResult result) {
                    BluetoothDevice device = result.getDevice();
                    Log.i("Fisken", "onAdvertisementUpdate " + result + " device: " + device);

                    AppConfig config = AppConfig.getInstance(TempLogScanner.this);
                    TempLogDeviceConfig.Device confDevice = config.deviceConfig.getDevice(device.getAddress());
                    if (confDevice == null) {
                        confDevice = TempLogDeviceConfig.Device.fromScanResult(result);
                        config.deviceConfig.add(confDevice);
                        Log.i("Fisken", "New device");
                    }

                    if (true) { // TODO: Add service extra flag for this
                        Intent service = new Intent(TempLogScanner.this, TempLogProfile.class);
                        service.putExtra("device_addr", device.getAddress());
                        TempLogScanner.this.startService(service);
                    }
                }

                @Override
                public void onScanFailed (int errorCode) {
                    Log.e("Fisken", "onScanFailed " + errorCode);
                }
            };
}
