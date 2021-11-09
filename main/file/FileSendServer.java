package main.file;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FileSendServer extends Thread{

    int port;
    File userFolder;

    public FileSendServer(int port, File userFolder) {
        this.port = port;
        this.userFolder = userFolder;
    }

    @Override
    public void run(){
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while(true) {
                Socket client = serverSocket.accept();
                System.out.println("Connection at: " + client.getRemoteSocketAddress());
                FileSegmentSender fileSegmentSender = new FileSegmentSender(client,userFolder);
                fileSegmentSender.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
