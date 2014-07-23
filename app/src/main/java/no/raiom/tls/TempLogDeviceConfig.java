package no.raiom.tls;

import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;

import java.util.ArrayList;

public class TempLogDeviceConfig extends ArrayList<TempLogDeviceConfig.Device> {

    public TempLogDeviceConfig() {
        add(new Device("fd:ed:32:a4:74:a2", "TLS_480206234", "Dev device"));
    }

    public Device getDevice(String addr) {
        for (TempLogDeviceConfig.Device device : this) {
            if (device.device.equals(addr)) {
                return device;
            }
        }
        return null;
    }

    public static class Device {
        public String device;
        public String name;
        public String desc;

        public Device(String device, String name, String desc) {
            this.device = device.toUpperCase();
            this.name   = name;
            this.desc   = desc;
        }

        public static Device fromScanResult(ScanResult result) {
            ScanRecord record = ScanRecord.parseFromBytes(result.getScanRecord());
            String addr = result.getDevice().getAddress();
            String device_name = record.getLocalName();
            return new Device(addr, device_name, "");
        }
    }
}
