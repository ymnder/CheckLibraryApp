package com.ymnd.app.checklibraryapp;

import android.app.Application;

import com.facebook.stetho.InspectorModulesProvider;
import com.facebook.stetho.Stetho;
import com.facebook.stetho.inspector.protocol.ChromeDevtoolsDomain;
import com.facebook.stetho.rhino.JsRuntimeReplFactoryBuilder;
import com.facebook.stetho.timber.StethoTree;
import com.github.pedrovgs.lynx.LynxShakeDetector;

import timber.log.Timber;

/**
 * Created by yamazaki on 2017/03/06.
 */

public class MyApplication extends Application {

    public static final int DUMMY = 1;

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
            Stetho.initialize(Stetho.newInitializerBuilder(this)
                    .enableDumpapp(Stetho.defaultDumperPluginsProvider(getApplicationContext()))
                    .enableWebKitInspector(new InspectorModulesProvider() {
                        @Override
                        public Iterable<ChromeDevtoolsDomain> get() {
                            return new Stetho.DefaultInspectorModulesBuilder(getApplicationContext()).runtimeRepl(
                                    new JsRuntimeReplFactoryBuilder(getApplicationContext())
                                            .importClass(R.class)
                                            .importPackage("android.content")
                                            // Pass to JavaScript: var foo = "bar";
                                            .addVariable("foo", "bar")
                                            .build()
                            ).finish();
                        }
                    })
                    .build());
            Timber.plant(new StethoTree());

            LynxShakeDetector lynxShakeDetector = new LynxShakeDetector(this);
            lynxShakeDetector.init();
        }
    }
}