package no.raiom.tls;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TempLogScanner {
    private final static UUID                       TLS_SERVI = UUID.fromString("000018fa-0000-1000-8000-00805f9b34fb");
    private android.bluetooth.le.BluetoothLeScanner scanner;
    private Map<String, Boolean>                    addrs;
    private AppConfig                               config;


    public TempLogScanner(AppConfig config) {
        this.config               = config;
        this.addrs                = new HashMap<String, Boolean>();
        BluetoothManager  manager = (BluetoothManager)config.app_context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter  adapter = manager.getAdapter();
        scanner = adapter.getBluetoothLeScanner();
    }

    public void startScan() {
        // TODO: Check if bluetooth is enabled!

        // Filters
        List<android.bluetooth.le.ScanFilter> filters = new ArrayList<android.bluetooth.le.ScanFilter>();
        android.bluetooth.le.ScanFilter.Builder filter_builder = new android.bluetooth.le.ScanFilter.Builder();
        filter_builder.setServiceUuid(new ParcelUuid(TLS_SERVI));
        filter_builder.setServiceData(ByteUtils.hexStringToByteArray("FE1801"));
        filters.add(filter_builder.build());

        // Settings
        android.bluetooth.le.ScanSettings.Builder builder = new android.bluetooth.le.ScanSettings.Builder();
        builder.setCallbackType(android.bluetooth.le.ScanSettings.CALLBACK_TYPE_ON_UPDATE);
        builder.setScanMode(android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_POWER);
        android.bluetooth.le.ScanSettings settings = builder.build();

        // Start scan
        Log.i("Fisken", "TempLogScanner.startScan: settings " + settings + " filters " + filters + " leScanCallback " + leScanCallback);
        scanner.startScan(filters, settings, leScanCallback);
    }

    public void stopScan() {
        Log.i("Fisken", "TempLogScanner.stopScan");
        scanner.stopScan(leScanCallback);
    }

    private android.bluetooth.le.ScanCallback leScanCallback =
            new android.bluetooth.le.ScanCallback() {

                @Override
                public void onAdvertisementUpdate(android.bluetooth.le.ScanResult result) {
                    BluetoothDevice device = result.getDevice();
                    Log.i("Fisken", "onAdvertisementUpdate " + result + " device: " + device);
                    if (! addrs.containsKey(device.getAddress())) {
                        addrs.put(device.getAddress(), true);

                        Log.i("Fisken", "New device");
                        //Intent service = new Intent(config.app_context, TempLogProfile.class);
                        //service.putExtra("device_addr", device.getAddress());
                        //config.app_context.startService(service);
                    }
                }

                @Override
                public void onScanFailed (int errorCode) {
                    Log.e("Fisken", "onScanFailed " + errorCode);
                }
            };
}
