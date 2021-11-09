package main.peers;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.*;

public class PeersTableMaintainingProcess extends Thread {

    public interface Listener{
        void onPeersTableChanged(HashMap<String, Integer> peersTable);
    }

    private final LinkedHashMap<String, Integer> peersTable = new LinkedHashMap<>();
    private final Stack<String> keysToRemove = new Stack<>();
    private String host;
    private int port;
    private Listener listener;
    private boolean peersTableChanged;

    public PeersTableMaintainingProcess(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public PeersTableMaintainingProcess(String host, int port, Listener listener) {
        this.host = host;
        this.port = port;
        this.listener = listener;
    }

    @Override
    public void run() {
        PeerConfirmationThread peerConfirmationThread = new PeerConfirmationThread(peersTable);
        peerConfirmationThread.start();
        while(true) {
            try {
                Thread.sleep(1000);
                synchronized (peersTable) {
                    for (Map.Entry<String, Integer> peer : peersTable.entrySet()) {
                        peer.setValue(peer.getValue() - 1);
                        if (peer.getValue() < 0) {
                            keysToRemove.push(peer.getKey());
                            peersTableChanged = true;
                        }
                    }
                    while(keysToRemove.size() > 0){
                        peersTable.remove(keysToRemove.pop());
                    }
                    if(peersTableChanged && listener != null){
                        peersTableChanged = false;
                        listener.onPeersTableChanged(peersTable);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    class PeerConfirmationThread extends Thread {

        protected final HashMap<String, Integer> peersTable;
        protected MulticastSocket socket;
        protected byte[] buffer = new byte[256];
        protected int peerIndex;

        public PeerConfirmationThread(HashMap<String, Integer> peersTable) {
            this.peersTable = peersTable;
        }

        @Override
        public void run() {
            try {
                socket = new MulticastSocket(24999);
                InetAddress group = InetAddress.getByName("228.1.1.1");
                socket.joinGroup(group);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                while (true) {
                    socket.receive(packet);
                    synchronized (peersTable) {
                        String peerId = new String(packet.getData(), 0, packet.getLength());
                        if(!peerId.equals(host + ":" + port)) {
                            if (peersTable.containsKey(peerId)) {
                                peersTable.replace(peerId, 6);
                            } else {
                                peersTable.put(peerId, 6);
                                if(listener != null){
                                    listener.onPeersTableChanged(peersTable);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                socket.close();
            }
        }

    }

    public LinkedHashMap<String, Integer> getPeersTable() {
        return peersTable;
    }
}

