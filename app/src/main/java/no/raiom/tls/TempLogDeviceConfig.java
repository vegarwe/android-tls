package no.raiom.tls;

import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;

import java.util.ArrayList;

public class TempLogDeviceConfig extends ArrayList<TempLogDeviceConfig.Device> {
    private ArrayList<DataSetChangedHandler> handlers;
    private static TempLogDeviceConfig instance = null;

    public TempLogDeviceConfig() {
        handlers = new ArrayList<DataSetChangedHandler>();
    }

    public Device getDevice(String addr) {
        for (TempLogDeviceConfig.Device device : this) {
            if (device.device.equals(addr)) {
                return device;
            }
        }
        return null;
    }

    public static TempLogDeviceConfig getInstance() {
        if (TempLogDeviceConfig.instance == null) {
            TempLogDeviceConfig.instance = new TempLogDeviceConfig();
        }

        return instance;
    }

    public interface DataSetChangedHandler {
        public void onDataSetChangedCallback();
    }

    public void registerDataSetChangedHandler(DataSetChangedHandler handler) {
        handlers.add(handler);
    }

    public void unregisterDataSetChangedHandler(DataSetChangedHandler handler) {
        if (handlers.contains(handler)) {
            handlers.remove(handler);
        }
    }

    private void notifyDataSetChanged() {
        for (DataSetChangedHandler handler : handlers) {
            handler.onDataSetChangedCallback();
        }
    }

    @Override
    public boolean add(Device d) {
        super.add(d);
        notifyDataSetChanged();
        return true;
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
