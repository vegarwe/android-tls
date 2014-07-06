package no.raiom.tls;

import android.content.Context;

/**
 * Created by vegarwe on 18.05.2014.
 */
public class AppConfig {
    private static AppConfig app_instance = null;

    public TempLogDeviceConfig deviceConfig;
    public Context             app_context;
    public static final String APP_KEY    = "3f0zvk7t1aib5dd";
    public static final String APP_SECRET = "tvdzxfe1dkpd694";

    private AppConfig(Context context) {
        app_context = context;
        this.deviceConfig = new TempLogDeviceConfig();
    }

    public static AppConfig getInstance(Context context) {
        if (AppConfig.app_instance == null) {
            AppConfig.app_instance = new AppConfig(context.getApplicationContext());
        }

        return app_instance;
    }
}
