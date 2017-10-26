package com.example.myapp.ViewModel;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import com.example.myapp.DataModel.IBluetoothInput;
import com.example.myapp.DataModel.ISettingsProvider;
import com.example.myapp.DataModel.SettingsProvider;
import com.example.myapp.*;
import com.example.myapp.Renderer.ISimpleRenderer;

import javax.inject.Inject;
import java.util.Set;
import java.util.UUID;


public class EcgFragment extends Fragment implements IUpdater, IHeartbeat {


    @Inject
    IBluetoothInput bluetoothInput;

    @Inject
    ISettingsProvider settingsProvider;

    @Inject
    ISimpleRenderer simpleRenderer;

    IDisplayImage displayImage;

    Activity activity;
    Switch swch;
    private final View.OnClickListener handleSwcgEvent = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {

            if (EcgFragment.this.settingsProvider.getDeviceName().equalsIgnoreCase(SettingsProvider.SelectDeviceName)) {
                EcgFragment.this.tryShowToast(String.format("Cannot connect. %s", SettingsProvider.SelectDeviceName));
                if (EcgFragment.this.swch != null)
                    EcgFragment.this.swch.setChecked(false);
                return;
            }

            if (!EcgFragment.this.bluetoothInput.testConnection(activity)) {

                if (EcgFragment.this.swch != null) {
                    EcgFragment.this.swch.setChecked(false);
                    StopHeartAnimation();
                }
            } else {
                if (EcgFragment.this.swch != null
                        && !EcgFragment.this.swch.isChecked()) {
                    EcgFragment.this.bluetoothInput.SetConnectionStatus(false);
                    StopHeartAnimation();
                }
            }

            if (EcgFragment.this.bluetoothInput.GetConnectionStatus()) {

                Set<BluetoothDevice> pairedDevices = EcgFragment.this.bluetoothInput.GetPairedDevices();

                // If there are paired devices
                if (pairedDevices.size() > 0) {
                    // Loop through paired devices
                    for (BluetoothDevice device : pairedDevices) {
                        // Add the name and address to an array adapter to show in a ListView
                        String deviceName = device.getName();
                        if (deviceName.equalsIgnoreCase(EcgFragment.this.settingsProvider.getDeviceName())) {
                            String address = device.getAddress();
                            String name = device.getName();
                            Log.i(device.getName() + "\n" + device.getAddress(), "");
                            Log.i(address + "\n" + name, "");
                            //http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html
                            EcgFragment.this.bluetoothInput.connect("BT_SERVER", address, UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                            StartHeartAnimation();
                        }
                    }
                    //this.bluetoothInput.listen("BT_SERVER", UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                }
            } else {
                if (EcgFragment.this.swch != null)
                    EcgFragment.this.swch.setChecked(false);
            }

        }
    };

    TextView heartbeatText;
    ImageView myAnimation;
    AnimationDrawable myAnimationDrawable;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        // If activity recreated (such as from screen rotate), restore
        // the previous article selection set by onSaveInstanceState().
        // This is primarily necessary when in the two-pane layout.
        if (savedInstanceState != null) {
            //    this.currentPosition = savedInstanceState.getInt(NoteFragment.ARG_POSITION);
        }
        View view = inflater.inflate(R.layout.ecg_tab, container, false);

        swch = (Switch) view.findViewById(R.id.switch1);
        heartbeatText = (TextView) view.findViewById(R.id.textHeartbeat);
        if (swch != null)
            swch.setChecked(this.bluetoothInput.GetConnectionStatus() && this.bluetoothInput.isUp());

        swch.setOnClickListener(handleSwcgEvent);
        this.bluetoothInput.setUpdater(this);
        this.simpleRenderer.setHeartbeat(this);
        myAnimation = (ImageView) view.findViewById(R.id.myanimation);
        myAnimationDrawable = (AnimationDrawable) myAnimation.getDrawable();

        if (this.bluetoothInput.GetConnectionStatus() && this.bluetoothInput.isUp())
            myAnimationDrawable.start();
        else
            myAnimationDrawable.stop();

        if (this.simpleRenderer.getHeartbeat() != null)
            Update(this.simpleRenderer.getHeartbeat().toString());

        return view;
    }

    void StartHeartAnimation() {
        myAnimation.post(
                new Runnable() {
                    @Override
                    public void run() {
                        myAnimationDrawable.start();
                    }
                });

    }

    void StopHeartAnimation() {
        myAnimation.post(
                new Runnable() {
                    @Override
                    public void run() {
                        myAnimationDrawable.stop();
                    }
                });

    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
        EcgApplication app = (EcgApplication) activity.getApplication();
        app.getObjectGraph().inject(this);
        if (((IDisplayImage) activity) != null) {
            this.displayImage = ((IDisplayImage) activity);
        }
    }

    void tryShowToast(String toastMessage) {
        if (this.displayImage != null)
            this.displayImage.showToast(toastMessage);
    }

    @Override
    public void Update() {
        if (swch != null)
            swch.setChecked(this.bluetoothInput.GetConnectionStatus() && this.bluetoothInput.isUp());
        if (this.bluetoothInput.GetConnectionStatus() && this.bluetoothInput.isUp())
            StartHeartAnimation();
        else
            StopHeartAnimation();
    }


    @Override
    public void Update(String heartbeat) {
        if (heartbeatText != null)
            heartbeatText.setText(heartbeat);
    }
}
