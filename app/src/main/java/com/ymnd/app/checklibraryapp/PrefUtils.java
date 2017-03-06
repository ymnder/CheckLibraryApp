package com.ymnd.app.checklibraryapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by yamazaki on 2017/03/06.
 */

public class PrefUtils {

    private static final String EMOJI_INDEX = "emoji_index";

    public static void setEmojiIndex(final Context context, int index) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt(EMOJI_INDEX, index).commit();
    }

    public static int getEmojiIndex(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(EMOJI_INDEX, 0);
    }
}
