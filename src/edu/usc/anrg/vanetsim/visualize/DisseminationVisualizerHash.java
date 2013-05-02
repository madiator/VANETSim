/**
 *
 */
package edu.usc.anrg.vanetsim.visualize;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

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
public class DisseminationVisualizerHash extends Applet implements Runnable{

  /**
   *
   */
  private static final long serialVersionUID = -8633495413896360865L;
  Configuration config;
  NodeManager nodeManager;
  DBManager dbManager;
  TimeManager timer;
  StorageManager storageManager;

  LocationManager locationManager;
  private final double maxLatitude = 39.97;
  private final double minLatitude = 39.62;
  private final double maxLongitude= 117.37;
  private final double minLongitude= 116.02;
  private final int MAX_X = 600;
  private final int MAX_Y = 350;
  int circleRadius = 10;
  HashMap<Integer, NodeEntry> locations;
  private Thread updateThread;
  private final int[] nodeArray;
  private final int nCoding, kCoding, blockSize;
  private static int count = 0;
  private BufferedImage bufferedImage;

  private final GraphicsConfiguration gConfig = GraphicsEnvironment
      .getLocalGraphicsEnvironment().getDefaultScreenDevice()
      .getDefaultConfiguration();

  @Override
  public void start() {
    setSize(MAX_X, MAX_Y);
    bufferedImage = create(MAX_X, MAX_Y, true);
    if ( updateThread == null ) {
        updateThread = new Thread(this);
        updateThread.start();
    }
  }
  public DisseminationVisualizerHash() {
    config = new Configuration("mongo");
    config.setDbName("beijingWC_NID");
    config.setCollName("locationsNormalized");
    int fileSize = 1000;
    config.setFileSize(fileSize);
    blockSize = 1;
    kCoding = fileSize/blockSize;

    config.setBlockSize(blockSize);
    config.setCapacity(1000);
    config.setNumFiles(1);
    config.setStorageRedundancy(30);
    nCoding = kCoding;
    boolean storageType = StorageManager.UNCODED;
    config.setCodedStorage(storageType);
    config.setUncodedDistributed(false);
    if(storageType==StorageManager.UNCODED) {
      config.setUncodedDistributed(false);
    }
    //nCoding = config.getStorageRedundancy()*kCoding;
    Application.init(config);
    nodeManager = new NodeManager(config);
    config.setNodeManager(nodeManager);
    dbManager = Application.getDBManager();
    timer = Application.getTimeManager();
    locationManager = new LocationManagerDB(config, true);
    nodeArray = nodeManager.getNodeArray();
    System.out.println("nodeArray size = "+nodeArray.length+", first element = "+nodeArray[0]);
    initStuff();
  }

  @Override
  public void paint(Graphics g) {
    NodeEntry nodeEntry;
    if(locations!=null) {
      //double totalDrawn = 0;
      for(int nodeID:nodeArray) {
        nodeEntry = locations.get(nodeID);
        if(nodeEntry==null)
          continue;
        //totalDrawn += drawNode(g, nodeEntry);
        drawNode(g, nodeEntry);
      }
    }
  }

  public void run() {
    timer.reset();
    while(timer.hasNext()) {
      locations = locationManager.getNodeEntriesByTimeMap(timer.getSeconds());
      System.out.print(timer.getTimeSlot()+", ");
      double totalStored = getStorageStatus(locations, 0);

      repaint();
      simulateContacts();
      storeImage();
      timer.inc();
    }
  }

  public void storeImage() {
    BufferedImage image = create(MAX_X, MAX_Y, true);
    Graphics2D g = image.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    NodeEntry nodeEntry;
    for(int nodeID:nodeArray) {
      nodeEntry = locations.get(nodeID);
      if(nodeEntry==null)
        continue;
      //totalDrawn += drawNode(g, nodeEntry);
      drawNode(g, nodeEntry);
    }
    g.dispose();
    try {
      ImageIO.write(image, "png", new File("/Users/Mahesh/Documents/Work/VANETAnim/Set2/uncodedfile-"+count+".png"));
      count++;
    } catch (IOException e) {
    }

  }

  public void simulateContacts() {
    int bandwidthLimitation = 0;
    List<ContactEpisode> contactEpisodes =
      dbManager.getEasyContactEpisodes(timer.getTimeSlot());

    for(ContactEpisode contactEpisode:contactEpisodes) {
      NodeEntry sinkNode = contactEpisode.getSink();
      NodeEntry destNode = contactEpisode.getNeighbor();

      bandwidthLimitation = (int) (contactEpisode.getContactDuration()/blockSize);
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
    float status = (float) node.getStorageStatus()/kCoding;
    g.setColor(Color.BLACK);
    g.drawOval(y, x, circleRadius, circleRadius);
    if((status>0)&&(status<1)) {
      //status = status>1?1:status;
      g.setColor(new Color(0, 0, 1f, status));
      g.fillOval(y, x, circleRadius, circleRadius);
    } else if(status>=1) {
      g.setColor(new Color(1f, 0, 0, 1f));
      g.fillOval(y, x, circleRadius, circleRadius);
    }
    return node.getStorageStatus();
  }

  //Initialize the lists

  public void initStuff() {

    storageManager = new StorageManager(config);
    Application.setStorageManager(storageManager);
    System.out.println("* " +
        "* * Initializing the Storage Manager..");
    storageManager.initializeStorage();
    System.out.println("* * * Storage Manager done storing files.\n" +
        "* * Done initializing the storage manager");


  }

  public double getStorageStatus(HashMap<Integer, NodeEntry> locationsTemp, int whichFile) {
    double totalStorage = 0;
    double numSatistiedNodes = 0;
    NodeEntry nodeEntry, locNodeEntry;
    for(int nodeID:nodeArray) {
      nodeEntry = new NodeEntry(nodeID, null, null);
      locNodeEntry = locations.get(nodeID);

      double storageStatus = storageManager.getStorageStatus(nodeEntry, whichFile);
      //System.out.print(storageStatus+",");
      /*if(storageStatus>1) {
        System.out.println("ERROR due to over storage" +
        		"by node "+nodeID+" which is storing" +
        				""+storageStatus);
        System.exit(-1);
      }*/
      if(storageStatus>=kCoding)
        numSatistiedNodes++;
      if(locNodeEntry!=null)
        locNodeEntry.setStorageStatus(storageStatus);
      totalStorage+= storageStatus;
    }
    //System.out.println();
    System.out.println(totalStorage+", "+numSatistiedNodes);
    return totalStorage;
  }

  private BufferedImage create(final int width, final int height,
      final boolean alpha) {
    BufferedImage buffer = gConfig.createCompatibleImage(width, height,
              alpha ? Transparency.TRANSLUCENT : Transparency.OPAQUE);
    return buffer;
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

