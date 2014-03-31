package no.raiom.tls;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.util.List;
import java.util.UUID;

public class TempLogDeviceAction {
    public final static UUID TEMP_LOG = UUID.fromString("000018fb-0000-1000-8000-00805f9b34fb");

    public void connect(Context context, BluetoothDevice device) {
        device.connectGatt(context, false, leGattCallback);
    }

    private final BluetoothGattCallback leGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i("Fisken", "Connected " + gatt);
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i("Fisken", "Disconnected " + gatt);
                gatt.close();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            for (BluetoothGattService srv : gatt.getServices()) {
                Log.d("Fisken", "srv:      " + srv.getUuid().toString());

                for (BluetoothGattCharacteristic chr : srv.getCharacteristics()) {
                    Log.d("Fisken", ". chr:    " + chr.getUuid().toString());

                    for (BluetoothGattDescriptor dscr : chr.getDescriptors()) {
                        Log.d("Fisken", ".   dscr: " + dscr.getUuid().toString());
                        if (TEMP_LOG.equals(chr.getUuid())) {
                            gatt.setCharacteristicNotification(chr, true);
                            dscr.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                            gatt.writeDescriptor(dscr);
                        }
                    }
                }
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.i("Fisken", "onDescriptorWrite");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.i("Fisken", "onCharacteristicChanged  Uuid: " + characteristic.getUuid()
                    + " data:" + Utils.toByteString(characteristic.getValue()));

            if (TEMP_LOG.equals(characteristic.getUuid())) {
                List<TempLogSample> samples = TempLogSample.decode_samples(characteristic.getValue());
                for (TempLogSample s : samples) {
                    Log.i("Fisken", "Sample: " + s);
                    break;
                }
            }
        }
    };
}

