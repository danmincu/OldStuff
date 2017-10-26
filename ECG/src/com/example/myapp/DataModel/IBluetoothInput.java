package com.example.myapp.DataModel;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import com.example.myapp.IToastMessages;
import com.example.myapp.IUpdater;

import java.util.Set;
import java.util.UUID;

public interface IBluetoothInput{

    void setToastMessenger(IToastMessages toastMessages);
    void setUpdater(IUpdater updater);

    boolean testConnection(Activity activity);
    void SetConnectionStatus(boolean status);
    boolean GetConnectionStatus();
    boolean isUp();
    Set<BluetoothDevice> GetPairedDevices();
    void listen(String name, UUID my_uuid);
    void connect(String name, String device_name, UUID my_uuid);


}
