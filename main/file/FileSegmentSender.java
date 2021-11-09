package main.file;

import java.io.*;
import java.net.*;

public class FileSegmentSender extends Thread{

    Socket client;
    DataOutputStream dos;
    DataInputStream dis;
    byte[] buffer;
    File userFolder;
    File requestedFile;
    long start;
    long end;

    public FileSegmentSender(Socket client,File userFolder) {
        this.client = client;
        this.userFolder = userFolder;
        buffer = new byte[500];
        try {
            dos = new DataOutputStream(client.getOutputStream());
            dis = new DataInputStream(client.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run(){
        try {
            int fileNameLength = dis.readInt();
            dis.read(buffer,0,fileNameLength);
            String requestedFileName = new String(buffer, 0, fileNameLength);
            start = dis.readLong();
            end = dis.readLong();
            requestedFile = new File(userFolder,requestedFileName);
            if (requestedFile.exists()) {
                long bytesToSend = end - start;
                long bytesSent = 0;
                long fileIndex = 0;
                FileInputStream fis = new FileInputStream(requestedFile);
                while (fileIndex < start) {
                    byte[] moveIndexBuffer;
                    if(start - fileIndex < Short.MAX_VALUE){
                        moveIndexBuffer = new byte[(int)(start - fileIndex)];
                    }
                    else{
                        moveIndexBuffer = new byte[Short.MAX_VALUE];
                    }
                    fis.read(moveIndexBuffer,0,moveIndexBuffer.length);
                    fileIndex += moveIndexBuffer.length;
                }
                while (bytesToSend - bytesSent > 0) {
                    if (bytesToSend - bytesSent > Constants.PACKET_MAX_LENGTH) {
                        buffer = new byte[Constants.PACKET_MAX_LENGTH];
                        bytesSent += Constants.PACKET_MAX_LENGTH;
                    } else {
                        buffer = new byte[(int) (bytesToSend - bytesSent)];
                        bytesSent = bytesToSend;
                    }
                    fis.read(buffer);
                    dos.write(buffer);
                }
                fis.close();
                dis.close();
                dos.close();
            }
            else{
                System.out.println("Requested file not found, path: " + requestedFile.getAbsolutePath());
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
