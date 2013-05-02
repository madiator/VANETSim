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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import edu.usc.anrg.vanetsim.Application;
import edu.usc.anrg.vanetsim.manager.DBManager;
import edu.usc.anrg.vanetsim.manager.LocationManager;
import edu.usc.anrg.vanetsim.manager.LocationManagerDB;
import edu.usc.anrg.vanetsim.manager.NodeManager;
import edu.usc.anrg.vanetsim.manager.TimeManager;
import edu.usc.anrg.vanetsim.support.Configuration;
import edu.usc.anrg.vanetsim.support.LonLat;
import edu.usc.anrg.vanetsim.support.NodeEntry;

/**
 * @author Maheswaran Sathiamoorthy
 *
 */
/*
 * Goal: Draw lines corresponding to the paths taken by the taxis.
 * Update (Jan 29 2013): Will try to get the plots written to images.
 *
 * Update (Jan 22 2013): This program doesn't completely draw the traces.
 * And I am discontinuing the work because it is not required any more.
 * I used http://www.gpsvisualizer.com/ instead. It allows up to about 8
 * traces to be drawn on Google maps and that is sufficient for me.
 */
public class MovementVisualizer  extends Applet implements Runnable{
  /* Beijing
  private static final double maxLatitude = 40.12;
  private static final double minLatitude = 39.80;
  private static final double maxLongitude= 116.63;
  private static final double minLongitude= 116.20;
  */

  private static final double maxLatitude = 41.89;
  private static final double minLatitude = 41.69;
  private static final double maxLongitude= -87.059;
  private static final double minLongitude= -88.00;

  private static final int MAX_X = 900;
  private static final int MAX_Y = 700;
  int circleRadius = 5;
  Configuration config;
  NodeManager nodeManager;
  DBManager dbManager;
  TimeManager timer;
  LocationManager locationManager;
  //NodeEntry prevNodeEntry, currentNodeEntry;
  //List<NodeEntry> nodeEntries;
  List<Line> drawList;
  private Thread updateThread;
  private BufferedImage bufferedImage;

  private final GraphicsConfiguration gConfig = GraphicsEnvironment
      .getLocalGraphicsEnvironment().getDefaultScreenDevice()
      .getDefaultConfiguration();

  public MovementVisualizer() {
    HashMap<Integer, NodeEntry> locations;
    int[] nodeArray;
    config = new Configuration("mongo");
    //config.setDbName("beijingfull");
    config.setDbName("chicagobus");
    config.setCollName("locations");
    config.setHostName("yourhostname.com");
    config.setPort(27017);
    Application.init(config);
    nodeManager = new NodeManager(config);
    config.setNodeManager(nodeManager);
    //dbManager = Application.getDBManager();
    timer = Application.getTimeManager();
    locationManager = new LocationManagerDB(config, true);
    nodeArray = nodeManager.getNodeArray();
    System.out.println("nodeArray size = "+nodeArray.length+", first element = "+nodeArray[0]);
    drawList = new ArrayList<Line>();
  }

  @Override
  public void start() {
    setSize(MAX_X,MAX_Y);

    if ( updateThread == null ) {
        updateThread = new Thread(this);
        updateThread.start();
    }
  }

  @Override
  public void paint(Graphics g) {
    /*while(true) {
      while(drawList.size()>0) {
        drawLine(g, drawList.remove(0));
      }
    }*/
  }

  @Override
  public void run() {
    NodeEntry prevNodeEntry, currentNodeEntry;
    BufferedImage image = create(MAX_X, MAX_Y, true);
    Graphics2D g = image.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    for(int nodeIndex = 50; nodeIndex < 100;//nodeManager.getNumNodes();
        nodeIndex++) {

      System.out.print(nodeIndex);
      List<NodeEntry> nodeEntries =
          locationManager.getNodeEntriesByNodeID(
              nodeManager.getNodeID(nodeIndex));
      prevNodeEntry = null;
      currentNodeEntry = null;
      System.out.println("number of entries = "+nodeEntries.size());
      for(NodeEntry node:nodeEntries) {
        if(prevNodeEntry == null) {
          prevNodeEntry = node;
          continue;
        }
        currentNodeEntry = node;
        if(!prevNodeEntry.getLonLat().equals(currentNodeEntry.getLonLat())) {
          //drawList.add(getLine(prevNodeEntry, currentNodeEntry));
          //System.out.println("draw list size = "+drawList.size());
          drawLine(g, getLine(prevNodeEntry, currentNodeEntry));
        }
        //repaint();
        prevNodeEntry = node;
      }}
      g.dispose();
      try {//-"+nodeIndex+"
        ImageIO.write(image, "png", new File("/Users/Mahesh/Documents/Work/VSimulator/chicagoall.png"));
      } catch (IOException e) {
      }
    //}
  }

  Line getLine(NodeEntry node1, NodeEntry node2) {
    LonLat lonlat1 = node1.getLonLat();
    LonLat lonlat2 = node2.getLonLat();
    //if(lonlat1.equals(lonlat2))
    //  return;

    int x1 = (int) Math.floor((lonlat1.getLon() - minLongitude)*(MAX_X/(maxLongitude - minLongitude)));
    int y1 = (int) Math.floor((lonlat1.getLat() - minLatitude)*(MAX_Y/(maxLatitude - minLatitude)));

    int x2 = (int) Math.floor((lonlat2.getLon() - minLongitude)*(MAX_X/(maxLongitude - minLongitude)));
    int y2 = (int) Math.floor((lonlat2.getLat() - minLatitude)*(MAX_Y/(maxLatitude - minLatitude)));

    return new Line(x1, y1, x2, y2);
  }


  public void drawLine(Graphics g, Line line) {

    //System.out.println(node.getLon()+","+ node.getLat());

    //System.out.println(lonlat.getLat()+", "+lonlat.getLat());
    //g.setColor(Color.BLUE);
    //g.fillOval(x, MAX_Y-y, circleRadius, circleRadius);
    //g.fillOval(line.x1, MAX_Y-line.y1, circleRadius, circleRadius);
    g.setColor(Color.BLACK);
    g.fillOval(line.x2, MAX_Y-line.y2, circleRadius, circleRadius);
    g.drawLine(line.x1, MAX_Y-line.y1, line.x2, MAX_Y-line.y2);
  }

  class Line {
    int x1, x2, y1, y2;
    Line(int x1, int y1, int x2, int y2) {
      this.x1 = x1;
      this.y1 = y1;
      this.x2 = x2;
      this.y2 = y2;
    }
  }

  private BufferedImage create(final int width, final int height,
      final boolean alpha) {
    BufferedImage buffer = gConfig.createCompatibleImage(width, height,
              alpha ? Transparency.TRANSLUCENT : Transparency.OPAQUE);
    return buffer;
  }

}
