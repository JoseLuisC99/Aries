package test;

import main.peers.AnnouncementProcess;
import main.peers.PeersTableMaintainingProcess;

public class MulticastTest {

    public static void main(String[] args){
        AnnouncementProcess announcementProcess = new AnnouncementProcess("127.0.0.1",Integer.parseInt(args[0]));
        announcementProcess.start();
        PeersTableMaintainingProcess peersTableMaintainingProcess = new PeersTableMaintainingProcess("127.0.0.1",Integer.parseInt(args[0]));
        peersTableMaintainingProcess.start();
    }

}
