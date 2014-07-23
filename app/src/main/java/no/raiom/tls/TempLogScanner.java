package no.raiom.tls;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class TempLogScanner {
    private final static UUID                       TLS_SERVI = UUID.fromString("000018fa-0000-1000-8000-00805f9b34fb");
    private AppConfig                               config;
    private BluetoothLeScanner scanner;

    public TempLogScanner(AppConfig config) {
        this.config              = config;
        BluetoothManager manager = (BluetoothManager)config.context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = manager.getAdapter();
        scanner                  = adapter.getBluetoothLeScanner();
    }

    public void startScan() {
        // TODO: Check if bluetooth is enabled!

        // Filters
        List<ScanFilter> filters = new ArrayList<ScanFilter>();
        ScanFilter.Builder filter_builder = new ScanFilter.Builder();
        filter_builder.setServiceUuid(new ParcelUuid(TLS_SERVI));
        filter_builder.setServiceData(ByteUtils.hexStringToByteArray("FE1801"));
        filters.add(filter_builder.build());

        // Settings
        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setCallbackType(ScanSettings.CALLBACK_TYPE_ON_UPDATE);
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
        ScanSettings settings = builder.build();

        // Start scan
        Log.i("Fisken", "TempLogScanner.startScan: settings " + settings + " filters " + filters + " leScanCallback " + leScanCallback);
        getScanner().startScan(filters, settings, leScanCallback);
    }

    public void stopScan() {
        Log.i("Fisken", "TempLogScanner.stopScan: " + leScanCallback);
        getScanner().stopScan(leScanCallback);
    }

    private BluetoothLeScanner getScanner() {
        //BluetoothManager  manager = (BluetoothManager)config.context.getSystemService(Context.BLUETOOTH_SERVICE);
        //BluetoothAdapter  adapter = manager.getAdapter();
        //return adapter.getBluetoothLeScanner();
        return scanner;
    }

    private ScanCallback leScanCallback =
            new ScanCallback() {

                @Override
                public void onAdvertisementUpdate(ScanResult result) {
                    //BluetoothDevice device = result.getDevice();
                    //Log.i("Fisken", "onAdvertisementUpdate " + result + " device: " + device);

                    //TempLogDeviceConfig.Device confDevice = config.deviceConfig.getDevice(device.getAddress());
                    //if (confDevice == null) {
                    //    confDevice = TempLogDeviceConfig.Device.fromScanResult(result);
                    //    config.deviceConfig.add(confDevice);

                    //    Log.i("Fisken", "New device");
                    //    //Intent service = new Intent(config.app_context, TempLogProfile.class);
                    //    //service.putExtra("device_addr", device.getAddress());
                    //    //config.app_context.startService(service);
                    //}
                }

                @Override
                public void onScanFailed (int errorCode) {
                    Log.e("Fisken", "onScanFailed " + errorCode);
                }
            };
}
