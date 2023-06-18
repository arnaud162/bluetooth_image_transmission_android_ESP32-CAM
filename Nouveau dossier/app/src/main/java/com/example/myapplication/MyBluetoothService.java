package com.example.myapplication;

import android.bluetooth.BluetoothSocket;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class MyBluetoothService {
    private static final String TAG = "MY_APP_DEBUG_TAG";
    private Handler handler; // handler that gets info from Bluetooth service
    private ConnectedThread mConnectedThread;

    public MyBluetoothService(Handler mHandler, BluetoothSocket mmSocket) throws FileNotFoundException {
        handler = mHandler;
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();

    }

    public void gheiqlui(){
        Log.i("ecriture du fichier", "h");
    }

    // Defines several constants used when transmitting messages between the
    // service and the UI.
    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;

        // ... (Add other message types here as needed.)
    }

    private byte[] mmBuffer; // mmBuffer store for the stream
    private  BluetoothSocket mmSocket = null;
    private  InputStream mmInStream = null;
    private  OutputStream mmOutStream = null;
    boolean currentlyReceivingImage = false;
    boolean aboutToReceiveSizeOfTheImage = false;
    public class ConnectedThread extends Thread {
        String baseFolder = "/storage/self/primary/bluetooth";
        File oldImage = new File(baseFolder+"/image_raw_format.jpg");

        int compteur = 0;
        int compteurBytes = 0;
        int sizeFOfTheImage=0;
        int nbrOfBytesReceived = 0;
        long startTime = 0;
        long endTime = 0;
        byte [] imageArrayOfbytes;
        byte [] imageArrayOfbytes2;
        InputStream imageInputStream =new ByteArrayInputStream("a".getBytes());
        SequenceInputStream sequenceInputStream = new SequenceInputStream(imageInputStream,imageInputStream);


        public ConnectedThread(BluetoothSocket socket) throws FileNotFoundException  {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            mmBuffer = new byte[1024];
            int numBytes=0; // bytes returned from read()
            List<Byte> imageListOfByte = new ArrayList<Byte>();

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    if(!currentlyReceivingImage){
                        numBytes = mmInStream.read(mmBuffer);
                    }
                    // Send the obtained bytes to the UI activity.
//                    try{
//                       sequenceInputStream = new SequenceInputStream(sequenceInputStream, mmInStream);
//
//                    }
//                    catch(Exception e){
//                        Log.i("ecriture du fichier","c'est mort je joindrai pas tes streams "+e);
//                    }
                    //imageInputStream = new java.io.SequenceInputStream(imageInputStream, mmInStream);
                    //int nbrOfBytesReceived = 0;
                    if(currentlyReceivingImage){
                        //compteur++;
                        boolean delete = oldImage.delete();
                        FileOutputStream fOut= new FileOutputStream(baseFolder+"/image_raw_format.jpg",true);

                        int bytesRead;
                        compteur=0;
                        compteurBytes=0;
                        Log.i("ecriture du fichier",compteur+" avt boucle while , connection type "+ mmSocket.getConnectionType());
                        byte[] buffer = new byte[ 1024];
                        while ( compteurBytes<sizeFOfTheImage) { //-300 quickfix for a strange bug
                            bytesRead = mmInStream.read(buffer);
                            compteur++;
                            compteurBytes+=bytesRead;
                            fOut.write(buffer, 0, bytesRead);
                            endTime = System.currentTimeMillis()-startTime;
                            Log.i("ecriture du fichier",compteur+" boucle while "+ bytesRead+" "+compteurBytes+" time "+endTime );
                        }
                        //endTime = System.currentTimeMillis()-startTime;
                        //Log.i("ecriture du fichier",""+compteur+" "+numBytes+" "+endTime);
                        fOut.close();
                        //nbrOfBytesReceived=sizeFOfTheImage;
//                        for (int i = 0; i < numBytes; i++) {
//                          //  //imageListOfByte.add(mmBuffer[i]);
//                            imageArrayOfbytes[nbrOfBytesReceived+i]= mmBuffer[i];
//                        }
//                        //System.arraycopy(mmBuffer,0, imageArrayOfbytes,nbrOfBytesReceived,numBytes);
//                        nbrOfBytesReceived+=numBytes;
                        //fOut.write(Arrays.copyOfRange(mmBuffer, 0, numBytes)); // to get rid of the end of the array, which is full of zeros


                        //if (nbrOfBytesReceived >= sizeFOfTheImage){
                            endTime = System.currentTimeMillis()-startTime;
                            //Log.i("ecriture du fichier","fin de faire joujou avec les arrays, temps ncsr "+endTime);
                            currentlyReceivingImage=false;
                            //byte[] bytes = ArrayUtils.toPrimitive(image.toArray(new Byte[image.size()]));
                            //byte [] imagebyte = new byte[imageListOfByte.size()];
                            //for (int i =0; i<imageListOfByte.size();i++){
                            //    imagebyte[i]=imageListOfByte.get(i);
                            //}
                            compteur=0;
                            sizeFOfTheImage=0;
                            nbrOfBytesReceived=0;
                            //byte[] bytes=  Bytes.toArray(list);
                            //Message readMsg = handler.obtainMessage(
                              //      MessageConstants.MESSAGE_READ, imagebyte.length, -1,
                                //    imagebyte);
//                            Message readMsg = handler.obtainMessage(
//                                    MessageConstants.MESSAGE_READ, imageArrayOfbytes.length, -1,
//                                    imageArrayOfbytes);
                            //OutputStream os = null;
                            //FileUtils.copy(imageInputStream,os);
//                            try{
//                                Log.i("ecriture du fichier", "juste avant tentat d'avoir un buffer");
//                            //imageInputStream = new ByteArrayInputStream("aaaah".getBytes());
                                //int grhte=sequenceInputStream.read(imageArrayOfbytes2);
                            Log.i("ecriture du fichier", "juste aores tentat d'avoir un buffer"+ endTime);
                            //fOut.write(imageArrayOfbytes2);
//                            }
//                            catch(Exception e){
//                                Log.i("ecriture du fichier","1ecriture du fichier depuis is a foire "+e);
//                            }
                        //to send an input stream to mainActivity
                        File initialFile = new File(baseFolder+"/image_raw_format.jpg");
                            InputStream targetStream = new FileInputStream(initialFile);
                            Message readMsg = handler.obtainMessage(
                                    MessageConstants.MESSAGE_READ, sizeFOfTheImage, -1,
                                    targetStream);
                            readMsg.sendToTarget();
                            endTime = System.currentTimeMillis()-startTime;
                            Log.i("ecriture du fichier","fin, temps ncsr "+endTime);
                        currentlyReceivingImage=false;
                        //}

                    }
                    else if(aboutToReceiveSizeOfTheImage){
                        startTime = System.currentTimeMillis();
                        String s = new String(mmBuffer, StandardCharsets.UTF_8);
                        //byte []  b= new byte[mmBuffer.length];
                        int i =0;
                        while (Character.isDigit(mmBuffer[i])){ //we parse the buffer until we find a non digit byte.
                            i++;
                        }
                        sizeFOfTheImage = Integer.parseInt(s.substring(0, i));
                        //int sizeOfTheImage = Integer.parseInt(new String(mmBuffer, StandardCharsets.UTF_8));
                        Log.i("ecriture du fichier","taille de l'iamge : "+sizeFOfTheImage);
                        mmBuffer = new byte[1024];
//                       imageArrayOfbytes = new byte[sizeFOfTheImage];
//                        imageArrayOfbytes2 = new byte[1024];
                        aboutToReceiveSizeOfTheImage = false;
                        currentlyReceivingImage = true;
//                        imageInputStream=new ByteArrayInputStream("".getBytes());
                        Message readMsg = handler.obtainMessage(
                                MessageConstants.MESSAGE_READ, numBytes, -1,
                                "receving image... "+sizeFOfTheImage+" bytes ");
                        readMsg.sendToTarget();
                        endTime = System.currentTimeMillis()-startTime;
                        Log.i("ecriture du fichier","fin de la reception de la taille : "+endTime);
                    }
                    else{
                        Message readMsg = handler.obtainMessage(
                                MessageConstants.MESSAGE_READ, numBytes, -1,
                                mmBuffer);
                        readMsg.sendToTarget();
                    }

                    if (mmBuffer[0]==112){ //112 : p
                        aboutToReceiveSizeOfTheImage = true;

                    }
//                    Message readMsg = handler.obtainMessage(
//                            MessageConstants.MESSAGE_READ, numBytes, -1,
//                            mmInStream);

                    //Log.i("msg received from bluetooth", mmBuffer.toString());

                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                } catch (Exception e) {
                    Log.i("ecriture du fichier","2ecriture du fichier depuis is a foire "+e);
                }
            }
        }
    }
        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
                aboutToReceiveSizeOfTheImage = true; //only moment when write() is called when a photo demand is sent to the ESP32
                // Share the sent message with the UI activity.
//                Message writtenMsg = handler.obtainMessage(
//                        MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
//                writtenMsg.sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                Message writeErrorMsg =
                        handler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                handler.sendMessage(writeErrorMsg);
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }

}
