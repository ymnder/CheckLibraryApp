package com.ymnd.app.checklibraryapp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.stetho.InspectorModulesProvider;
import com.facebook.stetho.Stetho;
import com.facebook.stetho.inspector.protocol.ChromeDevtoolsDomain;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.facebook.stetho.rhino.JsRuntimeReplFactoryBuilder;
import com.github.pedrovgs.lynx.LynxActivity;
import com.github.pedrovgs.lynx.LynxConfig;
import com.google.gson.Gson;
import com.readystatesoftware.chuck.ChuckInterceptor;
import com.squareup.picasso.Picasso;
import com.ymnd.app.checklibraryapp.model.UserSettings;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private static final int FONT_SIZE_BASE_INDEX = 5;
    private static final String URL = "https://api.github.com/emojis";

    TextView welcomeText;
    SeekBar seekBar;
    Switch nightModeSwitch;
    Button saveButton;
    Button emojiButton;
    ImageView emojiView;
    Button lynxButton;

    SQLiteHelper sqLiteHelper;
    OkHttpClient client;

    boolean isSwitchOn;
    Context hey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showDebugDBAddressLogToast(this);
//        hey = this;
//        Stetho.initialize(Stetho.newInitializerBuilder(hey)
//                .enableDumpapp(Stetho.defaultDumperPluginsProvider(hey))
//                .enableWebKitInspector(new InspectorModulesProvider() {
//                    @Override
//                    public Iterable<ChromeDevtoolsDomain> get() {
//                        return new Stetho.DefaultInspectorModulesBuilder(hey).runtimeRepl(
//                                new JsRuntimeReplFactoryBuilder(hey)
//                                        .importClass(R.class)
//                                        .importPackage("android.content")
//                                        // Pass to JavaScript: var foo = "bar";
//                                        .addVariable("flag", 0)
//                                        .build()
//                        ).finish();
//                    }
//                })
//                .build());

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (BuildConfig.DEBUG) {
            builder.addNetworkInterceptor(new StethoInterceptor());
            builder.addInterceptor(new ChuckInterceptor(this));
        }
        client = builder.build();
        UserSettings settings = null;
        try {
            makeDataBase();
            settings = getCache();
        } catch (SQLException e) {
            Timber.e("[SQLiteHelper] failed DB migrate", e);
        }

        welcomeText = (TextView) findViewById(R.id.welcomeText);
        seekBar = (SeekBar) findViewById(R.id.fontSizeSlider);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                welcomeText.setTextSize(progress * FONT_SIZE_BASE_INDEX);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        nightModeSwitch = (Switch) findViewById(R.id.nightModeSwitch);
        nightModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isSwitchOn = isChecked;
                if (isChecked) {
                    welcomeText.setTextColor(getResources().getColor(R.color.colorAccent));
                } else {
                    welcomeText.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
            }
        });
        saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserSettings userSettings = UserSettings.create(seekBar.getProgress(), isSwitchOn);
                try {
                    sqLiteHelper.getDao(UserSettings.class).createOrUpdate(userSettings);
                    Toast.makeText(MainActivity.this, "saved!!", Toast.LENGTH_SHORT).show();
                } catch (SQLException e) {
                    Timber.e("[SQLiteHelper] failed DB upgrade", e);
                }
            }
        });
        emojiButton = (Button) findViewById(R.id.getEmojiButton);
        emojiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getEmoji();
            }
        });
        emojiView = (ImageView) findViewById(R.id.emojiImage);
        lynxButton = (Button) findViewById(R.id.lynxButton);
        lynxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLynxActivity();
            }
        });

        if (settings != null) {
            seekBar.setProgress(settings.getFontSize());
            nightModeSwitch.setChecked(settings.isNightMode());
        }


    }

    private void getEmoji() {
        if (client == null) return;

        new AsyncTask<Void, Void, Response>() {

            @Override
            protected Response doInBackground(Void... params) {
                Request request = new Request.Builder()
                        .url(URL)
                        .build();
                Response response = null;
                try {
                    response = client.newCall(request).execute();
                } catch (IOException e) {
                    Timber.e(e, e.getMessage());
                }
                return response;
            }

            @Override
            protected void onPostExecute(Response response) {
                super.onPostExecute(response);
                Emoji json = new Gson().fromJson(response.body().charStream(), Emoji.class);
                int index = PrefUtils.getEmojiIndex(getApplicationContext());
                String url = getEmojiUrl(json, index);
                Picasso.with(getApplicationContext()).load(url).into(emojiView);

                PrefUtils.setEmojiIndex(getApplicationContext(), index+1);
            }
        }.execute();
    }


    private void makeDataBase() throws SQLException {
        sqLiteHelper = new SQLiteHelper(getApplicationContext());
        sqLiteHelper.getWritableDatabase();
        List<UserSettings> cache = sqLiteHelper.getDao(UserSettings.class).queryForEq(BaseColumns._ID, 1);
        if (cache.isEmpty()) {
            sqLiteHelper.getDao(UserSettings.class).createOrUpdate(UserSettings.createDefault());
            Timber.d("[SQLiteHelper] create initial settings model");
        } else {
            Timber.d("[SQLiteHelper] cache is existed. skip creating database");
        }
    }

    private UserSettings getCache() throws SQLException {
        List<UserSettings> cache = sqLiteHelper.getDao(UserSettings.class).queryForEq(BaseColumns._ID, 1);
        return cache.get(0);
    }

    public static class Emoji {
        String tada;
        String cool;
        String heart;
    }

    private String getEmojiUrl(Emoji json, int index){
        String url = "";
        int icon = index % 3;
        switch(icon) {
            case 0:
                url = json.tada;
                break;
            case 1:
                url = json.cool;
                break;
            case 2:
                url =  json.heart;
                break;
        }
        Timber.d("[icon] index: %d, icon: %d, url: %s", index, icon, url);
        return url;
    }

    private void openLynxActivity() {
        LynxConfig lynxConfig = new LynxConfig();
        lynxConfig.setMaxNumberOfTracesToShow(4000)
                .setFilter("SQL");

        Intent lynxActivityIntent = LynxActivity.getIntent(this, lynxConfig);
        startActivity(lynxActivityIntent);
    }

    public static void showDebugDBAddressLogToast(Context context) {
        if (BuildConfig.DEBUG) {
            try {
                Class<?> debugDB = Class.forName("com.amitshekhar.DebugDB");
                Method getAddressLog = debugDB.getMethod("getAddressLog");
                Object value = getAddressLog.invoke(null);
                Toast.makeText(context, (String) value, Toast.LENGTH_LONG).show();
                Timber.d("[address] %s", value.toString());
            } catch (Exception ignore) {

            }
        }
    }
}
