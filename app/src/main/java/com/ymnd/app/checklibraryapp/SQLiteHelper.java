package com.ymnd.app.checklibraryapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.ymnd.app.checklibraryapp.model.UserSettings;

import java.sql.SQLException;

import timber.log.Timber;

/**
 * Created by yamazaki on 2017/03/06.
 */

public class SQLiteHelper extends OrmLiteSqliteOpenHelper {

    public static final String DATABASE_NAME = "app_ymnd_ooo.db";
    private static final int DATABASE_VERSION = 11;

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        Timber.d("[SQLiteHelper] onCreate");
        try {
            TableUtils.createTableIfNotExists(connectionSource, UserSettings.class);
        } catch (SQLException e) {
            Timber.e(e, e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        Timber.d("[SQLiteHelper] onUpgrade");
    }
}
