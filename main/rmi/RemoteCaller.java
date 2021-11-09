package main.rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

public class RemoteCaller {

    String host;
    int port;

    public RemoteCaller(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public ArrayList<String[]> searchFileInPeer(String fileName) {
        try {
            Registry registry = LocateRegistry.getRegistry(host,port);
            RemoteMethods caller = (RemoteMethods) registry.lookup("remote_methods");
            return caller.searchFileAndGetInfo(fileName);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
            return null;
        }
    }
}
