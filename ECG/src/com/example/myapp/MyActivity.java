package com.example.myapp;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;
import com.example.myapp.DataModel.BluetoothInput;
import com.example.myapp.DataModel.IBluetoothInput;
import com.example.myapp.DropBox.IDropboxSession;
import com.example.myapp.Renderer.ISimpleRenderer;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MyActivity extends FragmentActivity implements IDisplayImage {

    @Inject
    IDropboxSession dropboxSession;
    @Inject
    ISimpleRenderer simpleRenderer;
    @Inject
    IBluetoothInput bluetoothInput;

    private EcgApplication app;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.app = (EcgApplication) getApplication();
        this.app.getObjectGraph().inject(this);
        simpleRenderer.setRenderer(this);
        bluetoothInput.setToastMessenger(this);

        setContentView(R.layout.main);
        //this initiates the tab control
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            SlidingTabsBasicFragment fragment = new SlidingTabsBasicFragment();
            //this.app.getObjectGraph().inject(fragment);
            //fragment.mainActivity = this;
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();
        }
    }

    @Override
    public void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }

    /**
     * this is called when return to your app from outside
     */
    protected void onResume() {
        super.onResume();
        try {
            this.dropboxSession.AuthenticateAndStore();
        } catch (Exception e) {
            showToast("Couldn't authenticate with Dropbox:" + e.getLocalizedMessage());
        }
    }

    public void buttonClick(View v) {
        //this.ecgRenderer.render();
        this.simpleRenderer.renderAsync(new ArrayList<Integer>());
    }


    @Override
    public void displayImage(Bitmap bitmap) {
        final ImageView imageView = (ImageView) findViewById(R.id.view);
        imageView.setImageBitmap(bitmap);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == BluetoothInput.REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            this.bluetoothInput.SetConnectionStatus(resultCode == RESULT_OK);
        }
    }
}



