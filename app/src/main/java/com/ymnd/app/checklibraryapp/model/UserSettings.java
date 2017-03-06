package com.ymnd.app.checklibraryapp.model;

import android.provider.BaseColumns;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by yamazaki on 2017/03/06.
 */

@DatabaseTable
public class UserSettings {

    public static final String FONT_SIZE = "fontSize";
    public static final String NIGHT_MODE = "nightMode";

    @DatabaseField(columnName = BaseColumns._ID, id = true)
    private Integer id;
    @DatabaseField(columnName = FONT_SIZE)
    private int fontSize;
    @DatabaseField(columnName = NIGHT_MODE)
    private boolean nightMode;

    /**
     * for ORMLite
     */
    @SuppressWarnings("unused")
    private UserSettings () {}

    public static UserSettings create(int fontSize, boolean nightMode) {
        UserSettings userSettings = new UserSettings();
        userSettings.id = 1;
        userSettings.fontSize = fontSize;
        userSettings.nightMode = nightMode;
        return userSettings;
    }

    public static UserSettings createDefault() {
        return create(3, false);
    }

    public int getFontSize() {
        return fontSize;
    }

    public boolean isNightMode() {
        return nightMode;
    }
}
