package no.raiom.tls;

import android.content.Context;

public class AppConfig {
    private static AppConfig instance = null;

    public static final String APP_KEY    = "3f0zvk7t1aib5dd";
    public static final String APP_SECRET = "tvdzxfe1dkpd694";

    public static AppConfig getInstance() {
        if (AppConfig.instance == null) {
            AppConfig.instance = new AppConfig();
        }

        return instance;
    }
}
