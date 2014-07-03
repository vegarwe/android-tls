package no.raiom.tls;

import java.util.ArrayList;

/**
 * Created by vegarwe on 18.05.2014.
 */
public class DeviceConfig extends ArrayList<DeviceConfig.Device> {

    public DeviceConfig() {
        add(new Device("fd:ed:32:a4:74:a2", "TLS_480206234", "Dev device"));
        add(new Device("11:22:33:44:55:66", "fjase", "Ehhhh, kanskjde.de?"));
        add(new Device("11:33:22:55:44:66", "flyndre", "Joda, men neida"));
        add(new Device("66:55:44:33:22:11", "flatfisk", "Nei, altsaa"));
    }

    public class Device {
        public String device;
        public String name;
        public String desc;

        public Device(String device, String name, String desc) {
            this.device = device.toUpperCase();
            this.name   = name;
            this.desc   = desc;
        }
    }
}