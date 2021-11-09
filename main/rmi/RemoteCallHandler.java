package main.rmi;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class RemoteCallHandler implements RemoteMethods {

    private File searchFolder;
    private String host;
    private int port;

    public RemoteCallHandler(File searchFolder) {
        this.searchFolder = searchFolder;
    }

    @Override
    public ArrayList<String[]> searchFileAndGetInfo(String fileName) throws RemoteException {
        ArrayList<String[]> filesInfo = new ArrayList<>();
        File[] searchFolderFiles = searchFolder.listFiles();
        for(File file: searchFolderFiles){
            if(file.getName().toLowerCase().contains(fileName.toLowerCase())){
                String[] fileInfo = new String[5];
                try {
                    fileInfo[0] = file.getName();
                    fileInfo[1] = MD5Checksum.getMD5Checksum(file);
                    fileInfo[2] = "" + file.length();
                    fileInfo[3] = host;
                    fileInfo[4] = "" + (port - 1);
                    filesInfo.add(fileInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return filesInfo;
    }

    public void createHandler(String host,int port) {
        this.host = host;
        this.port = port;
        try {
            System.setProperty("java.rmi.server.codebase","file:///" + System.getProperty("user.dir") + "\\src\\main\\rmi");
            System.setProperty("java.rmi.server.hostname",host);
            System.out.println("Creating register at: " + port);
            RemoteMethods remote = (RemoteMethods) UnicastRemoteObject.exportObject(this, port);
            Registry registry = java.rmi.registry.LocateRegistry.createRegistry(port);
            registry.bind("remote_methods", remote);
            System.out.println("Ready");
        } catch (Exception e) {
            System.out.println("Server exception: " + e.toString());
            e.printStackTrace();
        }

    }

}
