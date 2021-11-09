package main.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface RemoteMethods extends Remote {

    ArrayList<String[]> searchFileAndGetInfo(String fileName) throws RemoteException;

}
