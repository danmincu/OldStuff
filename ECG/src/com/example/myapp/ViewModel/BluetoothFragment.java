package com.example.myapp.ViewModel;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import com.example.myapp.DataModel.IBluetoothInput;
import com.example.myapp.DataModel.ISettingsProvider;
import com.example.myapp.DataModel.SettingsProvider;
import com.example.myapp.EcgApplication;
import com.example.myapp.IUpdater;
import com.example.myapp.R;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class BluetoothFragment extends Fragment implements IUpdater {

    @Inject
    IBluetoothInput bluetoothInput;

    @Inject
    ISettingsProvider settingsProvider;

    Activity activity;

    /*
    http://www.mkyong.com/android/android-spinner-drop-down-list-example/
     */
    private Spinner spinnerDevices;


    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        // If activity recreated (such as from screen rotate), restore
        // the previous article selection set by onSaveInstanceState().
        // This is primarily necessary when in the two-pane layout.
        if (savedInstanceState != null) {
            //    this.currentPosition = savedInstanceState.getInt(NoteFragment.ARG_POSITION);
        }
        View view = inflater.inflate(R.layout.bluetooth_tab, container, false);

        spinnerDevices = (Spinner) view.findViewById(R.id.spinnerDevices);
        List<String> list = new ArrayList<String>();
        list.add(SettingsProvider.SelectDeviceName);

        this.bluetoothInput.testConnection(activity);
        if (this.bluetoothInput.GetConnectionStatus()) {
            for (BluetoothDevice bt : this.bluetoothInput.GetPairedDevices()) {
                list.add(bt.getName());
            }
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDevices.setAdapter(dataAdapter);
        spinnerDevices.setSelection(list.indexOf(this.settingsProvider.getDeviceName()));
        spinnerDevices.setOnItemSelectedListener(new CustomOnItemSelectedListener());

        return view;
    }


    class CustomOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
            BluetoothFragment.this.settingsProvider.setDeviceName(parent.getItemAtPosition(pos).toString());
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }

    }


    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
        EcgApplication app = (EcgApplication) activity.getApplication();
        app.getObjectGraph().inject(this);
    }


    @Override
    public void Update() {

    }



}
