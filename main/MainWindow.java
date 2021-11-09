package main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import main.file.FileSegmentedDownloader;
import main.file.FileSendServer;
import main.peers.AnnouncementProcess;
import main.peers.PeersTableMaintainingProcess;
import main.rmi.RemoteCallHandler;
import main.rmi.RemoteCaller;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class MainWindow extends javafx.application.Application implements PeersTableMaintainingProcess.Listener{

    File userFolder;
    static int port;
    String host = "127.0.0.1";

    GridPane peersInfoPane;
    GridPane searchResultsPane;

    public static void main(String[] args){
        port = Integer.parseInt(args[0]);
        Application.launch(MainWindow.class);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setInitialDirectory(new File(System.getProperty("user.dir")));
        userFolder = dc.showDialog(primaryStage);
        //userFolder = new File(System.getProperty("user.dir"),"repository");

        VBox portSelector =  new VBox();
        portSelector.setAlignment(Pos.TOP_CENTER);
        portSelector.setSpacing(10);
        portSelector.setPrefWidth(800);
        portSelector.setPrefHeight(400);
        portSelector.setPadding(new Insets(125,0,0,0));
        Label portSelectorTitle = new Label("Introduce the port to run Aries");
        TextField portTextField = new TextField();
        portTextField.setPrefWidth(160);
        portTextField.setMaxWidth(160);
        portTextField.setPrefHeight(10);
        portTextField.setMaxHeight(10);
        portTextField.setAlignment(Pos.CENTER);
        Button portSelectorButton = new Button("Start");
        portSelectorButton.setPrefWidth(160);
        portSelectorButton.setMaxWidth(160);
        portSelectorButton.setOnMouseClicked(event -> {
            port = Integer.parseInt(portTextField.getText());
            AnnouncementProcess announcementProcess = new AnnouncementProcess(host,port);
            PeersTableMaintainingProcess peersTableMaintainingProcess = new PeersTableMaintainingProcess(host,port,this);
            FileSendServer fileSendServer = new FileSendServer(port, userFolder);
            RemoteCallHandler remoteCallHandler = new RemoteCallHandler(userFolder);
            announcementProcess.start();
            peersTableMaintainingProcess.start();
            fileSendServer.start();
            remoteCallHandler.createHandler(host,port + 1);

            TextField searchTextField = new TextField();
            searchTextField.setPrefWidth(385);
            searchTextField.setMaxWidth(386);
            searchTextField.setPrefHeight(10);
            searchTextField.setMaxHeight(10);
            searchTextField.setAlignment(Pos.CENTER);

            Button searchButton = new Button("Search");
            searchButton.setPrefWidth(185);
            searchButton.setMaxWidth(185);
            searchButton.setPrefHeight(10);
            searchButton.setMaxHeight(10);
            searchButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    Iterator<String> iterator = peersTableMaintainingProcess.getPeersTable().keySet().iterator();
                    ArrayList<String[]> searchResults = new ArrayList<>();
                    while(iterator.hasNext()) {
                        String[] peerInfo = iterator.next().split(":");
                        RemoteCaller remoteCaller = new RemoteCaller(peerInfo[0], Integer.parseInt(peerInfo[1]) + 1);
                        searchResults.addAll(remoteCaller.searchFileInPeer(searchTextField.getText()));
                    }
                    searchResultsPane.getChildren().removeIf( peerInfoLabel -> GridPane.getRowIndex(peerInfoLabel) > 0);
                    for(int i = 0; i < searchResults.size();i++){
                        String[] searchResult = searchResults.get(i);
                        for(int j = 0; j < 5;j++){
                            Label searchResultLabel = new Label(searchResult[j]);searchResultLabel.setPrefSize(100,10);
                            searchResultLabel.setAlignment(Pos.CENTER_LEFT);
                            searchResultsPane.add(searchResultLabel,j,i + 1);
                        }
                        Button downloadFileButton = new Button("Download");
                        downloadFileButton.setPrefSize(90,10);
                        downloadFileButton.setOnMouseClicked(new EventHandler<MouseEvent>() {

                            ArrayList<String[]> results = searchResults;
                            String[] result = searchResult;

                            @Override
                            public void handle(MouseEvent event) {
                                HashMap<String,Integer> matchResultsPeers = new HashMap<>();
                                for(int i = 0;i < results.size();i++){
                                    if(result[1].equals(results.get(i)[1])){
                                        matchResultsPeers.put(results.get(i)[3] + ":" + results.get(i)[4],0);
                                    }
                                }
                                FileSegmentedDownloader downloader = new FileSegmentedDownloader(userFolder,Integer.parseInt(result[2]),result[0],matchResultsPeers);
                                downloader.start();
                            }
                        });
                        searchResultsPane.add(downloadFileButton,5,i + 1);
                    }
                }
            });

            HBox searchMenu = new HBox();
            searchMenu.setSpacing(10);
            searchMenu.getChildren().addAll(searchTextField,searchButton);

            searchResultsPane = new GridPane();
            searchResultsPane.setHgap(10);
            searchResultsPane.setPadding(new Insets(10,0,0,0));
            searchResultsPane.setPrefSize(600,400);
            Label fileNameLabel = new Label("Name");
            fileNameLabel.setMinSize(90,10);
            fileNameLabel.setAlignment(Pos.CENTER_LEFT);
            searchResultsPane.add(fileNameLabel,0,0);
            Label fileLengthLabel = new Label("MD5");
            fileLengthLabel.setMinSize(90,10);
            fileLengthLabel.setAlignment(Pos.CENTER_LEFT);
            searchResultsPane.add(fileLengthLabel,1,0);
            Label fileMD5 = new Label("Length");
            fileMD5.setMinSize(90,10);
            fileMD5.setAlignment(Pos.CENTER_LEFT);
            searchResultsPane.add(fileMD5,2,0);
            Label fileHostLabel = new Label("Host");
            fileHostLabel.setMinSize(90,10);
            fileHostLabel.setAlignment(Pos.CENTER_LEFT);
            searchResultsPane.add(fileHostLabel,3,0);
            Label filePortLabel = new Label("Port");
            filePortLabel.setMinSize(90,10);
            filePortLabel.setAlignment(Pos.CENTER_LEFT);
            searchResultsPane.add(filePortLabel,4,0);

            VBox searchPane = new VBox();
            searchPane.setPrefSize(600,400);
            searchPane.setAlignment(Pos.TOP_CENTER);
            searchPane.setPadding(new Insets(10,10,10,10));
            searchPane.setSpacing(10);
            searchPane.getChildren().addAll(searchMenu, searchResultsPane);

            peersInfoPane = new GridPane();
            peersInfoPane.setPadding(new Insets(10,0,0,0));
            peersInfoPane.setPrefSize(200,400);
            Label hostTitle = new Label("Host");
            hostTitle.setMinSize(100,10);
            hostTitle.setAlignment(Pos.CENTER);
            peersInfoPane.add(hostTitle,0,0);
            Label portTitle = new Label("Port");
            portTitle.setMinSize(50,10);
            portTitle.setAlignment(Pos.CENTER);
            peersInfoPane.add(portTitle,1,0);

            HBox main = new HBox();
            main.setPrefSize(800,400);
            main.getChildren().addAll(peersInfoPane,searchPane);

            primaryStage.setScene(new Scene(main));
        });
        portSelector.getChildren().addAll(portSelectorTitle,portTextField,portSelectorButton);
        primaryStage.setScene(new Scene(portSelector));
        primaryStage.show();
        /*Event.fireEvent(portSelectorButton, new MouseEvent(MouseEvent.MOUSE_CLICKED,
                5, 5,5, 5, MouseButton.PRIMARY, 1,
                true, true, true, true, true, true, true, true, true, true, null));
        */
    }

    @Override
    public void onPeersTableChanged(HashMap<String, Integer> peersTable) {
        Platform.runLater( () -> {
            synchronized (peersTable) {
                peersInfoPane.getChildren().removeIf( peerInfoLabel -> GridPane.getRowIndex(peerInfoLabel) > 0);
                Iterator<String> iterator = peersTable.keySet().iterator();
                for (int i = 1; iterator.hasNext(); i++) {
                    String[] peerInfo = iterator.next().split(":");
                    Label hostTitle = new Label(peerInfo[0]);
                    hostTitle.setMinSize(100, 10);
                    hostTitle.setAlignment(Pos.CENTER);
                    peersInfoPane.add(hostTitle, 0, i);
                    Label portTitle = new Label(peerInfo[1]);
                    portTitle.setMinSize(50, 10);
                    portTitle.setAlignment(Pos.CENTER);
                    peersInfoPane.add(portTitle, 1, i);
                }
            }
        });
    }
}
