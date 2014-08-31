package no.raiom.tls;

import android.content.Context;

public class AppConfig {
    private static AppConfig app_instance = null;

    public TempLogDeviceConfig deviceConfig;
    public Context             context;
    public static final String APP_KEY    = "3f0zvk7t1aib5dd";
    public static final String APP_SECRET = "tvdzxfe1dkpd694";

    private AppConfig(Context context) {
        this.context = context;
        this.deviceConfig = new TempLogDeviceConfig();
    }

    public static AppConfig getInstance(Context context) {
        if (AppConfig.app_instance == null) {
            AppConfig.app_instance = new AppConfig(context.getApplicationContext());
        }

        return app_instance;
    }
}
