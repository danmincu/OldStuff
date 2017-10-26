package com.example.myapp.DataModel;

public interface ISettingsProvider
{
    void setPatientName(String patientName);
    String getPatientName();
    void setPatientStatus(String patientStatus);
    String getPatientStatus();
    void setDeviceName(String deviceName);
    String getDeviceName();
}
