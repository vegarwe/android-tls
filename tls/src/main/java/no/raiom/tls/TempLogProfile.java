package no.raiom.tls;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TempLogProfile extends Service {
    private final static UUID TLS_VALUE = UUID.fromString("000018fb-0000-1000-8000-00805f9b34fb");
    private final static UUID TLS_DESC1 = UUID.fromString("000018fc-0000-1000-8000-00805f9b34fb");
    private final static UUID TLS_DESC2 = UUID.fromString("000018fd-0000-1000-8000-00805f9b34fb");

    private Context             context;
    private byte[]              random;
    private int                 sample_interval;
    private int                 base_sample_num;
    private long                base_sample_ts;
    private List<TempLogSample> samples;

    BluetoothGattCharacteristic value_chr  = null;
    BluetoothGattDescriptor     value_dscr = null;
    BluetoothGattCharacteristic desc1_chr  = null;
    BluetoothGattCharacteristic desc2_chr  = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("Fisken", "TempLogDeviceSerivce: " + intent.getStringExtra("device_addr"));
        samples = new ArrayList<TempLogSample>();

        final BluetoothAdapter btAdapter =
                ((BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        BluetoothDevice device = btAdapter.getRemoteDevice(intent.getStringExtra("device_addr"));
        device.connectGatt(getBaseContext(), false, leGattCallback);
        this.context = this;

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e("Fisken", "TempLogProfile.onBind");
        return null;
    }

    private boolean has_all_handles() {
        return value_chr != null && value_dscr != null && desc1_chr != null && desc2_chr != null;
    }

    private void from_byte_data(long now, byte[] desc1, byte[] desc2) {
        random          = Arrays.copyOfRange(desc1, 0, 16);
//        sample_interval =       (desc1[16] & 0xff) + ((desc1[17] & 0xff) << 8);
//        base_sample_num =       (desc2[ 0] & 0xff) + ((desc2[ 1] & 0xff) << 8);
//        base_sample_ts  = now - (desc2[ 2] & 0xff) + ((desc2[ 3] & 0xff) << 8);
        sample_interval = ByteUtils.toUInt16(desc1, 16);
        base_sample_num = ByteUtils.toUInt16(desc2, 0);
        base_sample_ts  = now - ByteUtils.toUInt16(desc2, 2);
    }

    private void decode_samples(byte[] data) {
        byte flags            =  data[0];
        int  num_logs         = (data[1] & 0xff);
//        int  first_sample_num = ((data[2] & 0xff) + ((data[3] & 0xff) << 8));
        int  first_sample_num = ByteUtils.toUInt16(data, 2);
        for (int i = 0; i < num_logs; i++) {
            int    sample_num = first_sample_num + i;
//            double sample     = 0.0625 * ((data[4 + (2 * i)] & 0xff) + ((data[5 + (2 * i)] & 0xff) << 8));
            double sample     = 0.0625 * ByteUtils.toUInt16(data, 4 + (2*i));
            long   ts         = base_sample_ts + ((sample_num - base_sample_num) * sample_interval);
            samples.add(new TempLogSample(sample_num, sample, sample_num * sample_interval, ts));
        }
    }

    public class TempLogSample {
        private final int sample_number;
        private final double sample;
        private final long seconds_since_start;
        private final long ts;

        TempLogSample(int sample_number, double sample, long seconds_since_start, long ts) {
            this.sample_number = sample_number;
            this.sample = sample;
            this.seconds_since_start = seconds_since_start;
            this.ts = ts;
        }

        @Override
        public String toString() {
            return String.format("%4d;%3.2f;%s;%s",
                    sample_number, sample, seconds_since_start, ts);
        }
    }

    private final BluetoothGattCallback leGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.w("Fisken", "onConnectionStateChange: Wrong status (" + status + "), disconnect and close");
                gatt.disconnect();
                gatt.close();
                super.onConnectionStateChange(gatt, status, newState);
                return;
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i("Fisken", "Connected " + gatt);
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i("Fisken", "Disconnected " + gatt);
                gatt.close();

                TempLogApplication app = (TempLogApplication)getApplication();
                String filename = ByteUtils.bytesToHex(random) + ".csv";
                DropboxAppender dropbox = new DropboxAppender(getApplicationContext(), app.APP_KEY, app.APP_SECRET, filename);
                for (TempLogProfile.TempLogSample s : samples) {
                    dropbox.appendString(s.toString() + "\n");
                }
                dropbox.close();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.w("Fisken", "onServicesDiscovered: Wrong status (" + status + "), disconnect and close");
                gatt.disconnect();
                gatt.close();
                super.onServicesDiscovered(gatt, status);
                return;
            }

            for (BluetoothGattService srv : gatt.getServices()) {
                for (BluetoothGattCharacteristic chr : srv.getCharacteristics()) {
                    Log.i("Fisken", "UUID: " + chr.getUuid());
                    if (TLS_VALUE.equals(chr.getUuid())) {
                        value_chr = chr;
                        for (BluetoothGattDescriptor dscr : chr.getDescriptors()) {
                            value_dscr = dscr;
                        }
                    } else if (TLS_DESC1.equals(chr.getUuid())) {
                        desc1_chr = chr;
                    } else if (TLS_DESC2.equals(chr.getUuid())) {
                        desc2_chr = chr;
                    }
                }
            }

            if (has_all_handles()) {
                gatt.readCharacteristic(desc1_chr);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic chr,
                                         int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.w("Fisken", "onCharacteristicRead: Wrong status (" + status + "), disconnect and close");
                gatt.disconnect();
                gatt.close();
                super.onCharacteristicRead(gatt, chr, status);
                return;
            }

            Log.d("Fisken", "Read " + chr + " UUID " + chr.getUuid() + " value: " + ByteUtils.bytesToHex(chr.getValue()));
            if (TLS_DESC1.equals(chr.getUuid())) {
                gatt.readCharacteristic(desc2_chr);
            } else if (TLS_DESC2.equals(chr.getUuid())) {
                // Capture timestamp and decode templog meta data
                long now = System.currentTimeMillis();
                from_byte_data(now, desc1_chr.getValue(), desc2_chr.getValue());

                // Enable services
                gatt.setCharacteristicNotification(value_chr, true);
                value_dscr.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                gatt.writeDescriptor(value_dscr);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            if (TLS_VALUE.equals(characteristic.getUuid())) {
                decode_samples(characteristic.getValue());
            }
        }
    };
}
