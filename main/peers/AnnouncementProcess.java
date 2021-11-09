package main.peers;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class AnnouncementProcess extends Thread{

    private String host;
    private int port;

    public AnnouncementProcess(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() {
        try {
            DatagramSocket socket;
            InetAddress group;
            byte[] buf;
            socket = new DatagramSocket();
            String message = host + ":" + port;
            group = InetAddress.getByName("228.1.1.1");
            buf = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, group, 24999);
            while (true) {
                socket.send(packet);
                try {
                    Thread.sleep(5000);
                }
                catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
