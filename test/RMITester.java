package test;

import main.rmi.RemoteCallHandler;
import main.rmi.RemoteCaller;

import java.io.File;

public class RMITester {

    public static void main(String[] args) {
        RemoteCallHandler remoteCallHandler = new RemoteCallHandler(new File(System.getProperty("user.dir") + "\\repository"));
        remoteCallHandler.createHandler("127.0.0.1", 25000); // By some reason it works with port 1099
        RemoteCaller remoteCaller = new RemoteCaller("127.0.0.1", 25000);
        remoteCaller.searchFileInPeer("Prueba"); // If the default port is changed it needs to be indicated in get registry
    }
}
