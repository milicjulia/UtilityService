package com.iwedia.utility.stbremote;

import com.iwedia.utility.Logger;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import java.io.InputStream;
import java.io.OutputStream;
import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.UUID;
import java.io.IOException;

public class RemoteControlService extends Service {
    private static final Logger mLog = Logger.create(RemoteControlService.class);
    ArrayList<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track of all current registered clients.
    public static final int CMD__MOVE_UP = 16;
    public static final int CMD__MOVE_DOWN = 17;
    public static final int CMD__SOUND_PLUS = 25;
    public static final int CMD__SOUND_MINUS = 26;
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    public final static int COMMUNICATION_PORT = 2000;
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private String TAG = "juliam";
    private static  UUID myuuid= UUID.fromString("0000110a-0100-1000-8000-20805f9b34fb");

    @Override
    public void onCreate() {
        super.onCreate();
        mLog.d("juliam RemoteControlService onCreate");
		AcceptThread connect = new AcceptThread();
        connect.start();
        mLog.d("juliam RemoteControlService onCreate Service Started.");
    }

     private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        @SuppressLint("MissingPermission")
        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("STB",myuuid);
            } catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            Log.d("Acceptocket", "Run");
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                    Log.d("Acceptocket", "accept");
                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                  return;
                }
                new ConnectedThread(socket).start();
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }


    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private String ConnectedTag="juliam";

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(ConnectedTag, "Starting");
            mmSocket = socket;
            InputStream tmpIn = null;

            try {
                tmpIn = mmSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmInStream = tmpIn;
        }

        public void chUp() {
            try{
                Log.d("juliam","chUp");
                Runtime.getRuntime().exec("input keyevent "+ Commands.MOVE_UP);
            }catch(Exception e){}

        }

        public void chDown() {
            try{
                Log.d("juliam","chDown");
                Runtime.getRuntime().exec("input keyevent "+ Commands.MOVE_DOWN);
            }catch(Exception e){}
        }

        public void volUp() {
            try{
                Log.d("juliam","volUp");
                Runtime.getRuntime().exec("input keyevent "+ Commands.SOUND_PLUS);
            }catch(Exception e){}
        }

        public void volDown() {
            try{
                Log.d("juliam","volDown");
                Runtime.getRuntime().exec("input keyevent "+ Commands.SOUND_MINUS);
            }catch(Exception e){}
        }

        public void play(int command) {
            Log.d("juliam", "play 8->1");
            int toChannel = command - (int) (command / 10);

        }

        public void run() {
            int bytes;
            while (true) {
                try {
                    bytes = mmInStream.read();
                    switch (bytes) {
                        case 1:
                            chUp();
                            break;
                        case 2:
                            chDown();
                            break;
                        case 3:
                            volUp();
                            break;
                        case 4:
                            volDown();
                            break;
                        default:
                            play(bytes);
                            break;
                    }
                } catch (Exception e) {
                    Log.e("juliam", "write: Error reading Input Stream. " + e.getMessage());
                    break;
                }
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            if (mClients.size() == 0)
                mClients.add(msg.replyTo);

            super.handleMessage(msg);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mLog.d("juliam Received start id " + startId + ": " + intent);
      
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLog.d("juliam Service Stopped.");
    }

    public void sendMessageToUI(int msg_type, String msg_value) {
        try {
            Bundle b = new Bundle();
            b.putString("msg_value", "" + msg_value);
            Message msg = Message.obtain(null, msg_type);
            msg.setData(b);
            mClients.get(0).send(msg);
        } catch (RemoteException e) {
            mClients.remove(0);
        }
    }
}
