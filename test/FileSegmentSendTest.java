package test;

import main.file.FileSegmentReceiver;
import main.file.FileSegmentedDownloader;
import main.file.FileSendServer;

import java.io.File;
import java.util.HashMap;

public class FileSegmentSendTest {

    public static void main(String[] args){

        FileSendServer server = new FileSendServer(25000,new File(System.getProperty("user.dir") + "\\repository"));
        server.start();
        FileSendServer server2 = new FileSendServer(25001,new File(System.getProperty("user.dir") + "\\repository2"));
        server2.start();
        FileSendServer server3 = new FileSendServer(25002,new File(System.getProperty("user.dir") + "\\repository3"));
        server3.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        HashMap<String,Integer> peersTable = new HashMap<>();
        peersTable.put("127.0.0.1:25000",6);
        peersTable.put("127.0.0.1:25001",6);
        peersTable.put("127.0.0.1:25002",6);
        FileSegmentedDownloader fileReceiver = new FileSegmentedDownloader(new File(System.getProperty("user.dir") + "\\repository4"),4786605,"EFA.mp3",peersTable);
        fileReceiver.start();
        try {
            fileReceiver.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
