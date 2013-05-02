/**
 *
 */
package edu.usc.anrg.vanetsim.visualize;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.util.List;

import edu.usc.anrg.vanetsim.Application;
import edu.usc.anrg.vanetsim.manager.DBManager;
import edu.usc.anrg.vanetsim.manager.LocationManager;
import edu.usc.anrg.vanetsim.manager.LocationManagerDB;
import edu.usc.anrg.vanetsim.manager.NodeManager;
import edu.usc.anrg.vanetsim.manager.StorageManager;
import edu.usc.anrg.vanetsim.manager.TimeManager;
import edu.usc.anrg.vanetsim.support.Configuration;
import edu.usc.anrg.vanetsim.support.ContactEpisode;
import edu.usc.anrg.vanetsim.support.LonLat;
import edu.usc.anrg.vanetsim.support.NodeEntry;

/**
 * @author Maheswaran Sathiamoorthy
 *
 */
public class DisseminationVisualizer extends Applet implements Runnable{
  /**
   *
   */
  private static final long serialVersionUID = -1067070217529864384L;
  Configuration config;
  NodeManager nodeManager;
  DBManager dbManager;
  TimeManager timer;
  StorageManager storageManager;

  LocationManager locationManager;
  private final double maxLatitude = 40.0728981933594;
  private final double minLatitude = 39.5551991271973;
  private final double maxLongitude= 117.128997802734;
  private final double minLongitude= 115.521002197266;
  private final int MAX_X = 900;
  private final int MAX_Y = 700;
  int circleRadius = 10;
  List<NodeEntry> locations;
  private Thread updateThread;
  @Override
  public void start() {
    setSize(MAX_X,MAX_Y);

    if ( updateThread == null ) {
        updateThread = new Thread(this);
        updateThread.start();
    }
  }
  public DisseminationVisualizer() {
    config = new Configuration("mongo");
    config.setDbName("beijingWC_NID");
    config.setCollName("locationsNormalized");

    config.setFileSize(100);
    config.setCapacity(100);
    config.setNumFiles(1);
    config.setStorageRedundancy(30);

    Application.init(config);
    nodeManager = new NodeManager(config);
    config.setNodeManager(nodeManager);
    dbManager = Application.getDBManager();
    timer = Application.getTimeManager();

    //server = new Server(config, 1);
    locationManager = new LocationManagerDB(config, true);
    //NodeManager nodeManager = new NodeManager(config);
    //config.setNodeManager(nodeManager);
    //int numNodes = nodeManager.getNumNodes();

    init();


  }

  // begin testing the applet!
  @Override
  public void paint(Graphics g) {

    if(locations!=null) {
      double totalDrawn = 0;
      for(NodeEntry node:locations) {
        totalDrawn += drawNode(g, node);
      }
      System.out.println("TOTAL DRAWN = "+totalDrawn);
    }
  }

  public void run() {
    timer.reset();
    while(timer.hasNext()) {
      locations = locationManager.getNodeEntriesByTimeAsList(timer);
      double totalStored = getStorageStatus(locations, 0);
      System.out.println(timer.getTimeSlot()+" - "+totalStored +"-"+locations.size());
      repaint();
      simulateContacts();
      timer.inc();
    }
  }

  public void simulateContacts() {
    int bandwidthLimitation = 0;
    List<ContactEpisode> contactEpisodes =
      dbManager.getEasyContactEpisodes(timer.getTimeSlot());

    for(ContactEpisode contactEpisode:contactEpisodes) {
      NodeEntry sinkNode = contactEpisode.getSink();
      NodeEntry destNode = contactEpisode.getNeighbor();

      bandwidthLimitation = (int) (contactEpisode.getContactDuration());
      int fileToDownload = 0;

      storageManager.copyFile(sinkNode,
              destNode,
              fileToDownload,
              bandwidthLimitation/2);

     storageManager.copyFile(destNode,
           sinkNode,
           fileToDownload,
           bandwidthLimitation/2);
    }
  }

  public double drawNode(Graphics g, NodeEntry node) {
    //System.out.println(node.getLon()+","+ node.getLat());
    LonLat lonlat = node.getLonLat();
    int x = (int) Math.floor((lonlat.getLon() - minLongitude)*(MAX_X/(maxLongitude - minLongitude)));
    int y = (int) Math.floor((lonlat.getLat() - minLatitude)*(MAX_Y/(maxLatitude - minLatitude)));
    //System.out.println(node.getStorageStatus());
    if(node.getStorageStatus()>=1) {
      g.setColor(Color.BLUE);
      g.fillOval(y, x, circleRadius, circleRadius);
    } else if(node.getStorageStatus()>0){
      g.setColor(Color.RED);
      g.fillOval(y, x, circleRadius, circleRadius);
      System.out.println("painting in red!");
    }
    else {
      g.setColor(Color.BLACK);
      g.drawOval(y, x, circleRadius, circleRadius);
    }
    return node.getStorageStatus();
  }

  //Initialize the lists
  @Override
  public void init() {

    storageManager = new StorageManager(config);
    Application.setStorageManager(storageManager);
    System.out.println("* Initailzing the server...\n" +
        "* * Initializing the Storage Manager..");
    storageManager.initializeStorage();
    System.out.println("* * * Storage Manager done storing files.\n" +
        "* * Done initializing the storage manager");
    System.out.println("* Done initializing the server");

  }

  public double getStorageStatus(List<NodeEntry> nodeEntries, int whichFile) {
    double totalStorage = 0;
    for(NodeEntry nodeEntry:nodeEntries) {
      double storageStatus = storageManager.getStorageStatus(nodeEntry, whichFile);
      nodeEntry.setStorageStatus(storageStatus);
      totalStorage+= storageStatus;
    }
    return totalStorage;
  }
}



/*
 * From locationFull, mysql:
 * Max latitude = 42.7412986755371
 * Min latitude = 32.1222991943359
 * Max longitude= 126.154998779297
 * Min longitude= 111.658996582031
 *
 * From location, mysql (632 nodes)
 * Max latitude = 40.4768981933594
 * Min latitude = 39.4501991271973
 * Max longitude= 117.128997802734
 * Min longitude= 112.121002197266
 */
