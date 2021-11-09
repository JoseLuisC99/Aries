package main.file;

import sun.misc.IOUtils;
import sun.nio.ch.ThreadPool;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FileSegmentedDownloader extends Thread{

    private long fileLength;
    private FileSegmentReceiver[] receivers;
    private HashMap<String,Integer> peersTable;
    private File userFolder;
    private String fileName;

    public FileSegmentedDownloader(File userFolder,long fileLength,String fileName,HashMap<String,Integer> peersTable) {
        this.fileLength = fileLength;
        this.userFolder = userFolder;
        this.fileName = fileName;
        this.peersTable = peersTable;
    }

    @Override
    public void run(){
        int peersAvailable = peersTable.size();
        receivers = new FileSegmentReceiver[peersAvailable];
        ExecutorService pool = Executors.newFixedThreadPool(peersAvailable);
        Iterator<String> peersIterator = peersTable.keySet().iterator();
        for(int i = 0;peersIterator.hasNext();i++)
        {
            String host[] = peersIterator.next().split(":");
            FileSegmentReceiver segmentReceiver;
            if(peersIterator.hasNext()){
                segmentReceiver = new FileSegmentReceiver(host[0],Integer.parseInt(host[1]),fileName,(fileLength/peersAvailable) * i,((fileLength/peersAvailable) * (i + 1)) - 1,i);
            }
            else{
                segmentReceiver = new FileSegmentReceiver(host[0],Integer.parseInt(host[1]),fileName,(fileLength/peersAvailable) * i,fileLength,i);
            }
            receivers[i] = segmentReceiver;
            pool.execute(segmentReceiver);
        }
        pool.shutdown();
        try {
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Join files");
        File target = new File(userFolder,fileName);
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(target));
            byte[] buffer = new byte[Short.MAX_VALUE];
            for (int i = 0; i < peersAvailable; i++) {
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(receivers[i].getTarget()));
                while (bis.available() > 0) {
                    int bytesRead = bis.read(buffer,0,buffer.length);
                    bos.write(buffer,0,bytesRead);
                }
                bis.close();
            }
            bos.close();
            /*for (int i = 0; i < peersAvailable; i++) {
                receivers[i].getTarget().delete();
            }*/
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("File merged");
    }

}
