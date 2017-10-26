package com.example.myapp;

import android.app.Application;
import android.content.SharedPreferences;
import dagger.ObjectGraph;

/*
class to maintain a global application state.
 */
public class EcgApplication extends Application {

    private ObjectGraph objectGraph;
    //SharedPreferences settings;

    @Override
    public void onCreate()
    {
        super.onCreate();
        //Initializes the settings variable
        //this.settings = getSharedPreferences("Settings", MODE_PRIVATE);
        Object[] modules = new Object[] {
                new EcgDataModule(this)
        };
        objectGraph = ObjectGraph.create(modules);
    }

    public ObjectGraph getObjectGraph() {
        return this.objectGraph;
    }


}

