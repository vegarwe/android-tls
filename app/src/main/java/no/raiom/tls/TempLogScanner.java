package no.raiom.tls;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

public class TempLogScanner {
    private final static UUID                       TLS_SERVI = UUID.fromString("000018fa-0000-1000-8000-00805f9b34fb");
    private BluetoothAdapter                        mBluetoothAdapter;
    private Map<String, Boolean>                    addrs;
    private AppConfig                               mConfig;


    public TempLogScanner(AppConfig config) {
        mConfig           = config;
        addrs             = new HashMap<String, Boolean>();
        mBluetoothAdapter = ((BluetoothManager)config.app_context.getSystemService(
                Context.BLUETOOTH_SERVICE)).getAdapter();
    }


    public void startScan() {
        // Filters
        List<android.bluetooth.le.ScanFilter> filters = new ArrayList<android.bluetooth.le.ScanFilter>();
        //android.bluetooth.le.ScanFilter.Builder filter_builder = new android.bluetooth.le.ScanFilter.Builder();
        //filter_builder.setServiceUuid(new ParcelUuid(TLS_SERVI));
        //filters.add(filter_builder.build());

        // Settings
        android.bluetooth.le.ScanSettings.Builder builder = new android.bluetooth.le.ScanSettings.Builder();
        builder.setCallbackType(android.bluetooth.le.ScanSettings.CALLBACK_TYPE_ON_UPDATE);
        builder.setScanMode(android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_POWER);
        android.bluetooth.le.ScanSettings settings = builder.build();

        // Start scan
        android.bluetooth.le.BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();
        scanner.startScan(filters, settings, leScanCallback);
        Log.i("Fisken", "TempLogScanner.startScan " + scanner + " settings " + settings + " filters " + filters + " leScanCallback " + leScanCallback);
    }

    public void stopScan() {
        android.bluetooth.le.BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();
        scanner.stopScan(leScanCallback);
        Log.i("Fisken", "TempLogScanner.stopLeScan");
    }

    private android.bluetooth.le.ScanCallback leScanCallback =
            new android.bluetooth.le.ScanCallback() {

                @Override
                public void onAdvertisementUpdate(android.bluetooth.le.ScanResult result) {
                    Log.i("Fisken", "onAdvertisementUpdate " + result);
                    //if (! addrs.get(device.getAddress())) {
                    //    addrs.put(device.getAddress(), true);

                    //    Intent service = new Intent(TempLogScanner.this, TempLogProfile.class);
                    //    service.putExtra("device_addr", device.getAddress());
                    //    startService(service);
                    //}
                }

                @Override
                public void onScanFailed (int errorCode) {
                }
            };
}
