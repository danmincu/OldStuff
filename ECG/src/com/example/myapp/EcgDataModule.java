package com.example.myapp;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.example.myapp.DataModel.*;
import com.example.myapp.DropBox.DropboxSession;
import com.example.myapp.DropBox.IDropboxSession;
import com.example.myapp.Location.ILocation;
import com.example.myapp.Location.Location;
import com.example.myapp.Renderer.ISimpleRenderer;
import com.example.myapp.Renderer.SimpleRenderer;
import com.example.myapp.ViewModel.BluetoothFragment;
import com.example.myapp.ViewModel.EcgFragment;
import com.example.myapp.ViewModel.HistoryFragment;
import com.example.myapp.ViewModel.PatientFragment;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module(complete = true, injects = { MyActivity.class, SlidingTabsBasicFragment.class, PatientFragment.class,
        HistoryFragment.class, BluetoothFragment.class, EcgFragment.class})
public class EcgDataModule {

    private final Context appContext;
    public static boolean sourceToggle = false;
    private EcgApplication parent;
    private Handler uiHandler;

    /** Constructs this module with the application context. */
    public EcgDataModule(EcgApplication app) {
        this.parent = app;
        this.appContext = app.getApplicationContext();
        this.uiHandler = new Handler(Looper.getMainLooper());
    }

    ISettingsProvider patientProvider;
    @Provides
    ISettingsProvider getPatientProvider()
    {
        if (this.patientProvider == null)
            this.patientProvider = new SettingsProvider(this.appContext);
        return this.patientProvider;
    }

    IDropboxSession dropboxSession;
    @Provides
    IDropboxSession getDropboxSession()
    {
        if (dropboxSession == null)
            dropboxSession = new DropboxSession(this.appContext);
        return this.dropboxSession;
    }

    IImageStorage imageStorage;
    @Provides
    IImageStorage getImageStorage()
    {
        if (this.imageStorage == null)
            this.imageStorage = new ImageStorage(this.appContext, this.getPatientProvider());
        return this.imageStorage;
    }

    IBluetoothInput bluetoothInput;
    @Provides
    IBluetoothInput getBluetoothInput()
    {
        if (this.bluetoothInput == null)
            this.bluetoothInput = new BluetoothInput(this.appContext, this.uiHandler, this.getSimpleRenderer());
        return this.bluetoothInput;
    }

    ISimpleRenderer simpleRenderer = null;
    @Provides
    ISimpleRenderer getSimpleRenderer()
    {
        if(this.simpleRenderer == null)
          this.simpleRenderer = new SimpleRenderer(this.appContext, this.getImageStorage(),this.getDropboxSession(), this.getLocation());
        return this.simpleRenderer;
    }

    ILocation location = null;
    @Provides
    ILocation getLocation()
    {
        if(this.location == null)
            this.location = new Location(this.appContext);
        return this.location;
    }



}


