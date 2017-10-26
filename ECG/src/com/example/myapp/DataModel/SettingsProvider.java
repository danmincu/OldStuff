package com.example.myapp.DataModel;

import android.content.Context;
import android.content.SharedPreferences;


public class SettingsProvider implements ISettingsProvider {

    SharedPreferences settings;
    public SettingsProvider(Context appContext)
    {
        //Initializes the settings variable
        this.settings = appContext.getSharedPreferences("Settings", Context.MODE_PRIVATE);

    }

    @Override
    public void setPatientName(String patientName) {
        SharedPreferences.Editor editor = this.settings.edit();
        editor.putString("PatientName", patientName);
        editor.commit();
    }

    @Override
    public String getPatientName() {
        return this.settings.getString("PatientName", "John Doe");
    }

    @Override
    public void setDeviceName(String deviceName){
        SharedPreferences.Editor editor = this.settings.edit();
        editor.putString("DeviceName", deviceName);
        editor.commit();
    }

    public static String SelectDeviceName = "Select a device name...";

    @Override
    public String getDeviceName(){
        //clear everything this.settings.edit().clear().commit();
        return this.settings.getString("DeviceName", SettingsProvider.SelectDeviceName);
    }

    @Override
    public void setPatientStatus(String patientStatus) {
        SharedPreferences.Editor editor = this.settings.edit();
        editor.putString("PatientStatus", patientStatus);
        editor.commit();
    }

    @Override
    public String getPatientStatus() {
        return this.settings.getString("PatientStatus", "resting");
    }
}
