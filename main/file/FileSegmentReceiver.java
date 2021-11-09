package main.file;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

public class FileSegmentReceiver extends Thread{

    private DataOutputStream dos;
    private DataInputStream dis;
    private String fileName;
    private File target;
    private long start;
    private long end;

    public FileSegmentReceiver(String host, int port,String fileName,long start,long end,int segmentIndex) {
        this.start = start;
        this.end = end;
        this.fileName = fileName;
        System.out.println("Index: " + segmentIndex);
        target = new File("temp" + segmentIndex);
        try {
            Socket client = new Socket(host,port);
            dos = new DataOutputStream(client.getOutputStream());
            dis = new DataInputStream(client.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run(){
        try {
            byte[] buffer;
            dos.writeInt(fileName.getBytes().length);
            dos.flush();
            dos.write(fileName.getBytes());
            dos.flush();
            dos.writeLong(start);
            dos.flush();
            dos.writeLong(end);
            dos.flush();

            long bytesReceived = 0;
            long bytesToReceive = end - start;
            FileOutputStream fos = new FileOutputStream(target);
            while (bytesToReceive - bytesReceived > 0) {
                if (bytesToReceive - bytesReceived > Constants.PACKET_MAX_LENGTH) {
                    buffer = new byte[Constants.PACKET_MAX_LENGTH];
                    bytesReceived += Constants.PACKET_MAX_LENGTH;
                } else {
                    buffer = new byte[(int) (bytesToReceive - bytesReceived)];
                    bytesReceived = bytesToReceive;
                }
                dis.read(buffer);
                fos.write(buffer);
            }
            fos.close();
            System.out.println("Path: " + target.getAbsolutePath());
            dis.close();
            dos.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public File getTarget() {
        return target;
    }
}
