package no.raiom.tls;

import android.app.Activity;
import android.app.KeyguardManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

public class TempLogDeviceAction {
    private KeyguardManager.KeyguardLock mKeyGuardLock;
    private boolean keyguard_disabled = false;
    private boolean found_device      = false;

    private BluetoothDevice m_fisken_dev;

    private Context mContext;

    public TempLogDeviceAction(Context context) {
        mContext = context;
    }

    public BluetoothAdapter.LeScanCallback leScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    Log.e("Fisken", "Found device: "
                            + device.getName() + " " + device.getAddress());
                    if (device.getName().equals("TLS_480204226")) {
                        device.connectGatt(mContext, true, mGattCallback);
                    }
                }
            };

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("Fisken", "onConnectionStateChange");
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i("Fisken", "Disconnected from GATT server.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.i("Fisken", "onServicesDiscovered");
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            Log.i("Fisken", "onCharacteristicRead");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.i("Fisken", "onCharacteristicChanged");
        }
    };
}

