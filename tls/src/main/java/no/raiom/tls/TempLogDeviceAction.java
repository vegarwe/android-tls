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
    public final static UUID TLS_VALUE = UUID.fromString("000018fb-0000-1000-8000-00805f9b34fb");
    public final static UUID TLS_DESC1 = UUID.fromString("000018fc-0000-1000-8000-00805f9b34fb");
    public final static UUID TLS_DESC2 = UUID.fromString("000018fd-0000-1000-8000-00805f9b34fb");

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public void connect(Context context, BluetoothDevice device) {
        device.connectGatt(context, false, leGattCallback);
    }

    private class TlsServiceHandles {
        BluetoothGattCharacteristic value_chr  = null;
        BluetoothGattDescriptor     value_dscr = null;
        BluetoothGattCharacteristic desc1_chr  = null;
        BluetoothGattCharacteristic desc2_chr  = null;
        TempLog                     tempLog    = null;

        public boolean has_all_handles() {
            return value_chr != null && value_dscr != null && desc1_chr != null && desc2_chr != null;
        }
    }

    private final BluetoothGattCallback leGattCallback = new BluetoothGattCallback() {
        TlsServiceHandles                 tls = null;

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.w("Fisken", "Wrong status, disconnect and close");
                gatt.disconnect();
                gatt.close();
                super.onConnectionStateChange(gatt, status, newState);
                return;
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i("Fisken", "Connected " + gatt);
                tls = new TlsServiceHandles();
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i("Fisken", "Disconnected " + gatt);
                gatt.close();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.w("Fisken", "onServicesDiscovered received: " + status);
                return;
            }

            for (BluetoothGattService srv : gatt.getServices()) {
                for (BluetoothGattCharacteristic chr : srv.getCharacteristics()) {
                    Log.i("Fisken", "UUID: " + chr.getUuid());
                    if (TLS_VALUE.equals(chr.getUuid())) {
                        tls.value_chr = chr;
                        for (BluetoothGattDescriptor dscr : chr.getDescriptors()) {
                            tls.value_dscr = dscr;
                        }
                    } else if (TLS_DESC1.equals(chr.getUuid())) {
                        tls.desc1_chr = chr;
                    } else if (TLS_DESC2.equals(chr.getUuid())) {
                        tls.desc2_chr = chr;
                    }
                }
            }

            if (tls.has_all_handles()) {
                gatt.readCharacteristic(tls.desc1_chr);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic chr,
                                            int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.w("Fisken", "onServicesDiscovered received: " + status);
                return;
            }

            Log.d("Fisken", "Read " + chr + " UUID " + chr.getUuid() + " value: " + bytesToHex(chr.getValue()));
            if (TLS_DESC1.equals(chr.getUuid())) {
                gatt.readCharacteristic(tls.desc2_chr);
            } else if (TLS_DESC2.equals(chr.getUuid())) {
                long now = System.currentTimeMillis();
                tls.tempLog = TempLog.from_byte_data(now, tls.desc1_chr.getValue(), tls.desc2_chr.getValue());
                gatt.setCharacteristicNotification(tls.value_chr, true);
                tls.value_dscr.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                gatt.writeDescriptor(tls.value_dscr);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            if (TLS_VALUE.equals(characteristic.getUuid())) {

                List<TempLog.TempLogSample> samples = tls.tempLog.decode_samples(characteristic.getValue());
                for (TempLog.TempLogSample s : samples) {
                    StringBuilder sl = new StringBuilder();
                    sl.append(" rand: ");
                    sl.append(bytesToHex(tls.tempLog.random));
                    sl.append(" sample: ");
                    sl.append(s);
                    Log.i("Fisken", sl.toString());
                }
            }
        }
    };
}

