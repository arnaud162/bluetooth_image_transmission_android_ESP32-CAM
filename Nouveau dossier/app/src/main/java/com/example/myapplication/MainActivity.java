package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    long t1 =0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textViewErrors = (TextView) findViewById(R.id.textViewErrors);
        TextView textViewInfos = (TextView) findViewById(R.id.textViewInfos);
        textViewInfos.setMovementMethod(new ScrollingMovementMethod());
        TextView textViewListOfDevices = (TextView) findViewById(R.id.textViewListOfDevices);
        Button buttonConnectToESP32 = findViewById(R.id.buttonConnectToESP32);
        Button buttonTakePhoto = findViewById(R.id.buttonTakePhoto);
        ImageView receivedImage = findViewById(R.id.receivedImage);
        buttonTakePhoto.setEnabled(false);

        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        final boolean[] currentlyReceivingImage = {false};
        boolean ESP32CAMwasFound = false;


        if (bluetoothAdapter == null) {
            textViewErrors.setText("Device doesn't support Bluetooth");
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            int REQUEST_ENABLE_BT = 0;
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            textViewInfos.setText("bluetoothAdapter isEnabled");
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        BluetoothDevice mmDevice = null;
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                textViewListOfDevices.setText(textViewListOfDevices.getText() + deviceName +" "+deviceHardwareAddress+ "\n");
//                if (device.getName().equals("arnaud-UX410UQK")) {
//                    //if (device.getName().equals("DESKTOP-4DDPA66")) {
//                    mmDeviceLaptop = device;
//                    textViewInfos.setText("laptop detected, connection attempt will be made on laptop and not ESP32");
//                    mmDevice = device;
//                    break;
//                }
                if (device.getName().equals("ESP32-CAM")) {
                    mmDevice = device;
                    textViewInfos.setText("ESP32-CAM found. you can now click the connect button !");
                    buttonConnectToESP32.setEnabled(true);
                    ESP32CAMwasFound=true;

                } else if(!ESP32CAMwasFound){
                        mmDevice = device;
                    buttonConnectToESP32.setEnabled(false);
                    textViewInfos.setText("no device with name ESP32-CAM was found. Have you paired the ESP32-CAM in the bluetooth settings ?");}

            }
        }

        //here i should do discovery. won't, its complicated

        // following code makes device discoverable. useless
//        int requestCode = 1;
//        Intent discoverableIntent =
//                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
//        startActivityForResult(discoverableIntent, requestCode);

        final BluetoothSocket mmSocket;
// to getacess to declaration of funtions listenUsingInsecureL2capChannel() and accept()
//        BluetoothServerSocket bsc = bluetoothAdapter.listenUsingInsecureL2capChannel();
//        bsc.accept();

        BluetoothSocket tmp = null;
        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = mmDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            //tmp = mmDevice.createInsecureL2capChannel(0x1001);
            //tmpLaptop = mmDeviceLaptop.createInsecureL2capChannel(0x1001);
            //tmp = mmDevice.createL2capChannel(0x1001);
        } catch (IOException e) {
            textViewErrors.setText("Socket's create() method failed" + e);
        }
        mmSocket = tmp;

        final MyBluetoothService[] myBluetoothService = {null};
        buttonConnectToESP32.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //bluetoothAdapter.cancelDiscovery();
                try {
                    // Connect to the remote device through the socket. This call blocks
                    // until it succeeds or throws an exception.
                    mmSocket.connect();
                    textViewInfos.setText("apparently conneciton was succesful");
                    buttonTakePhoto.setEnabled(true);
//                    AcceptThread ac = new AcceptThread();
//                    ac.start();


                    //constants for the handler
                    final int MESSAGE_STATE_CHANGE = 1;
                    final int MESSAGE_READ = 2;
                    final int MESSAGE_WRITE = 3;
                    final int MESSAGE_DEVICE_NAME = 4;
                    final int MESSAGE_TOAST = 5;

                    final Handler mHandler = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
//                            try {
//                                mmInStream.read(readBuf);
//                            } catch (IOException e) {
//                                textViewErrors.setText("impossible to convert inputstreqm to byte[]");
//                            }
                            String readMessage="";

                            if (!currentlyReceivingImage[0]){
                                try{
                                    byte[] readBuf = (byte[]) msg.obj;
                                    readMessage = new String(readBuf, 0, msg.arg1);
                                    readMessage = "received : "+readMessage;
                                }
                                catch(Exception e){
                                    currentlyReceivingImage[0]=true; // when a string is received, an image is about to be received
                                    t1 = System.currentTimeMillis();
                                    readMessage = (String)msg.obj;
                                }
                                textViewListOfDevices.setText(textViewListOfDevices.getText()+readMessage+"\n");
                            }
                            else{
                                //byte[] readBuf = (byte[]) msg.obj;
                                //Bitmap b = BitmapFactory.decodeByteArray(readBuf,0, msg.arg1);
                                Bitmap b = BitmapFactory.decodeStream((InputStream) msg.obj);
                                receivedImage.setImageBitmap(b);
                                currentlyReceivingImage[0]=false;
                                long timeOfTransmission = System.currentTimeMillis()-t1;
                                textViewListOfDevices.setText(textViewListOfDevices.getText()+"Succesfully received image in " +timeOfTransmission+" ms.\n");
                            }
//                            else{
//                                //textViewListOfDevices.setText(textViewListOfDevices.getText()+"received"+readMessage+"\n");
//                                textViewListOfDevices.setText("received : "+readMessage+"\n");
//                            }

//                            switch (msg.what) {
//                                case MESSAGE_READ:
//                                    byte[] readBuf = (byte[]) msg.obj;
//                                    String readMessage = new String(readBuf, 0, msg.arg1);
//                                    textViewListOfDevices.setText(readMessage);
//                                    break;
//                                default:
//                                    textViewListOfDevices.setText(textViewListOfDevices.getText()+"\nstuff happened. but what"+msg.what);
//                                    break;
//                            }
                        }
                    };

                    myBluetoothService[0] = new MyBluetoothService(mHandler, mmSocket);

//
//                    ExampleThread thread = new ExampleThread(mHandler, mmSocket);
//                    thread.start();

                    } catch (IOException e) {
                        // Unable to connect; close the socket and return.
                        textViewErrors.setText("Unable to connect; close the socket and return "+e);
                        try {
                            mmSocket.close();
                        } catch (IOException closeException) {
                            textViewErrors.setText(textViewErrors.getText()+"\nSocket's create() method failed"+  closeException);
                        }
                        return;
                    }
                }
            }
        );

            buttonTakePhoto.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    myBluetoothService[0].write("{\"st\":3}".getBytes());
                    t1 = System.currentTimeMillis();
                    //currentlyReceivingImage[0]=true;
                    //mmSocket.
    //                try {
    //                mmSocket.close();
    //                textViewInfos.setText("disconected with sucess");
    //                currentlyReceivingImage[0]=false;
    //            } catch (IOException e) {
    //                textViewErrors.setText( "Could not close the client socket "+ e);
    //            }
                }
            }
        );



    }




}

//class ExampleThread extends Thread{
//
//        ExampleThread(Handler mHandler, BluetoothSocket mmSocket) {
//            MyBluetoothService myBluetoothService = new MyBluetoothService(mHandler, mmSocket);
//        }
//
////        public void run(){
////            myBluetoothService
////        }
//}

