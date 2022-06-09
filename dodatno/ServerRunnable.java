package com.iwedia.utility.stbremote;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.net.ServerSocket;
import java.net.Socket;

import com.iwedia.utility.Logger;
public class ServerRunnable implements Runnable {

    private static final String TAG = "juliam";
      private static final Logger mLog = Logger.create(ServerRunnable.class);

    private int port;
      Thread client_thread;

    private RemoteControlService rcs;
    ClientRunnable client_runnable;

    public ServerRunnable(RemoteControlService serv, int communicationPort) {
        port = communicationPort;
        rcs = serv;
        mLog.v("juliam ServerRunnable constructor");

    }

    @Override
    public void run() {

        try {

            mLog.i("juliam Starting server...");

            ServerSocket s = new ServerSocket(port);
            s.setReuseAddress(true);

            mLog.i("juliam Server is listening");
            boolean first=true;

            while (s != null) {
                final Socket sock_client = s.accept();
                mLog.i("juliam new client: " + sock_client.hashCode());
                // launch a new thread for each new client - This thread will handle client
                // commands/request from the remote control
                if(first){
                    first=false;
                      client_runnable = new ClientRunnable(sock_client, rcs);
                 client_thread = new Thread(client_runnable);
                client_thread.start();
                }
            }

        } catch (Exception e) {
            mLog.e("juliam ERROR:");
        }
    }
}