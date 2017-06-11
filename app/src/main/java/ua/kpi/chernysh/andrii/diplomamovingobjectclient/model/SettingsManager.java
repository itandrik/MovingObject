package ua.kpi.chernysh.andrii.diplomamovingobjectclient.model;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by 1 on 07.06.2017.
 */

public class SettingsManager {
    public static final String SHARED_PREFERENCES_NAME = "MovingObject";
    public static final String LOCALE = "locale";

    private Context context;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public SettingsManager(Context context) {
        if (context != null) {
            this.context = context;
            this.preferences = context.getSharedPreferences(SettingsManager.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
            this.editor = preferences.edit();
        }
    }


    public String getLocale() {
        return preferences.getString(LOCALE, "ua");
    }

    public void setLocale(String lang) {
        editor.putString(LOCALE, lang).apply();
    }
}
