package com.example.myapp.DataModel;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import com.example.myapp.IToastMessages;
import com.example.myapp.IUpdater;
import com.example.myapp.Renderer.ISimpleRenderer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * http://developer.android.com/guide/topics/connectivity/bluetooth.html
 * http://stackoverflow.com/questions/23963815/sending-data-from-android-to-arduino-with-hc-06-bluetooth-module
 */

//http://www.vogella.com/tutorials/AndroidBackgroundProcessing/article.html

public class BluetoothInput implements IBluetoothInput {

    public static int TRIGGER_UI_SAMPLE_LENGTH = 4000;
    public final static int REQUEST_ENABLE_BT = 1;
    private static final String TAG = "bluetooth input";
    boolean btThreadRunning = false;
    Context context;
    Handler uiHandler;
    ISimpleRenderer simpleRenderer;
    IUpdater updater;
    ConcurrentLinkedQueue<Byte> outQueue = new ConcurrentLinkedQueue<Byte>();
    ConnectedThread connectedThread = null;
    private boolean btEnabled;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    private IToastMessages toastMessenger;


    public BluetoothInput(Context context, Handler uiHandler, ISimpleRenderer simpleRenderer) {
        this.context = context;
        this.uiHandler = uiHandler;
        this.simpleRenderer = simpleRenderer;
    }

    public void setToastMessenger(IToastMessages toastMessenger) {
        this.toastMessenger = toastMessenger;
    }

    public void setUpdater(IUpdater updater) {
        this.updater = updater;
    }

    @Override
    public boolean testConnection(Activity activity) {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            this.btEnabled = false;
            return false;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            this.btEnabled = false;
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return false;
        }
        this.btEnabled = true;
        return true;
    }

    @Override
    public void SetConnectionStatus(boolean status) {
        btEnabled = status;
        if (!btEnabled) {
            btThreadRunning = false;
            if (connectedThread != null) {
                connectedThread.cancel();
            }
            connectedThread = null;
        }
    }

    public boolean GetConnectionStatus() {
        return this.btEnabled;
    }

    public boolean isUp() {
        return this.btThreadRunning;
    }

    public Set<BluetoothDevice> GetPairedDevices() {
        if (this.btEnabled) {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
                return mBluetoothAdapter.getBondedDevices();
            }
        }
        return null;
    }

    public void listen(String name, UUID my_uuid) {
        if (this.btEnabled) {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
                (new AcceptThread(mBluetoothAdapter, name, my_uuid)).start();
            }
        }
    }

    public void connect(String name, String device_name, UUID my_uuid) {
        if (this.btEnabled) {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
                //(new AcceptThread(mBluetoothAdapter, name, my_uuid)).start();
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(device_name);
                try {
                    btSocket = device.createRfcommSocketToServiceRecord(my_uuid);
                    mBluetoothAdapter.cancelDiscovery();
                    outQueue.clear();

                    //new connection to a given device is being spawned
                    connectedThread = new ConnectedThread(btSocket, outQueue);
                    connectedThread.start();

                    //create a consumer for the queue that
                    //syncs data with the main UI thread using the uiHandler
                    Runnable queueConsumerRunnable = new Runnable() {
                        public void run() {

                            ArrayList<Byte> list = new ArrayList<Byte>();

                            while (btThreadRunning || !outQueue.isEmpty()) {

                                try {
                                    Thread.sleep(50);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }


                                while (!outQueue.isEmpty()) {
                                    Byte value = outQueue.poll();
                                    if (value != null)
                                        list.add(value);
                                    if (list.size() >= TRIGGER_UI_SAMPLE_LENGTH * 2)
                                        break;
                                }

                                class MyRunnable implements Runnable {
                                    ArrayList<Integer> byteList;

                                    public MyRunnable(ArrayList<Integer> byteList) {
                                        this.byteList = byteList;
                                    }

                                    public void run() {
                                        if (BluetoothInput.this.simpleRenderer != null)
                                            BluetoothInput.this.simpleRenderer.renderAsync(byteList);
                                    }
                                }

                                if (!list.isEmpty() && list.size() > TRIGGER_UI_SAMPLE_LENGTH * 2 && list.size() % 2 == 0) {

                                    //don't clean for continuous recording
                                    // outQueue.clear();
                                    ArrayList<Integer> values = new ArrayList<Integer>();
                                    int odd = 1;
                                    int hiByte = 0;
                                    for (Byte b : list) {
                                        if (odd > 0) {
                                            if (b < 0)
                                                hiByte = (int) 256 - b;
                                            else
                                                hiByte = b;
                                        } else {
                                            if (b < 0)
                                                values.add((int) ((hiByte << 8) + 256 + b));
                                            else
                                                values.add((int) ((hiByte << 8) + b));
                                        }
                                        odd *= -1;
                                    }
                                    BluetoothInput.this.uiHandler.post(new MyRunnable(values));
                                    list.clear();
                                }
                            }
                        }
                    };

                    Thread queueConsumerThread = new Thread(queueConsumerRunnable);
                    queueConsumerThread.start();

                } catch (IOException e) {
                    btThreadRunning = false;
                }
            }
        }
    }


    /*
      This thread is responsible to be the "waiting to connect" bluetooh server that
      spawns a ConnectedThread once a connection has been established.
      **NOT used in this implementation**
     */
    class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread(BluetoothAdapter mBluetoothAdapter, String name, UUID my_uuid) {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(name, my_uuid);
            } catch (IOException e) {
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = mmServerSocket.accept(10000);
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    //todo manageConnectedSocket(socket);
                    (new ConnectedThread(socket, outQueue)).start();

                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        /**
         * Will cancel the listening socket, and cause the thread to finish
         */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
            }
        }
    }

    /*
      This thread is responsible to entertain the bluetooth socket communication
      as of now runs infinitely unless an IOException occurs
     */
    class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        boolean connectionProblem =false;

        public ConnectedThread(BluetoothSocket socket, ConcurrentLinkedQueue<Byte> outQueue) {
            mmSocket = socket;
            if (!mmSocket.isConnected())
                try {
                    mmSocket.connect();
                } catch (IOException e) {
                    connectionProblem = true;
                    e.printStackTrace();
                    try {
                        mmSocket.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }

            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        void connectionDown() {
            BluetoothInput.this.uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (BluetoothInput.this.updater != null)
                        BluetoothInput.this.updater.Update();
                }
            });
            this.cancel();
        }
        //https://developer.android.com/training/multiple-threads/communicate-ui.html
        public void run() {
            if (connectionProblem)
                return;
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            btThreadRunning = true;
            // Keep listening to the InputStream until an exception occurs
            while (true) {

                if (!btThreadRunning) {
                    connectionDown();
                }

                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (bytes > 0) {
                        //loop back
                        //disabled for now mmOutStream.write(buffer, 0, bytes);
                        int iterations = 0;
                        for (byte b : buffer) {
                            outQueue.add(b);
                            iterations++;
                            if (iterations >= bytes)
                                break;
                        }
                    }
                    // Send the obtained bytes to the UI activity
                    //todo mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                    btThreadRunning = false;
                    connectionDown();
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    btThreadRunning = false;
                    connectionDown();
                    e.printStackTrace();
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

}

