/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.usc.anrg.vanetsim.support;

import org.joda.time.DateTime;



/**
 * A container class to store details of nodes.
 * Useful when making database transactions.
 * @author Maheswaran Sathiamoorthy
 */
public class NodeEntry {
    private int nid;
    private final LonLat lonlat;
    private final int t;
    private double distance; //distance from some other node.
    private final DateTime timestamp;
    private double storageStatus;

    public double getLon() {
      return lonlat.getLon();
    }

    public double getLat() {
      return lonlat.getLat();
    }

    public int getTime() {
      return t;
    }

    public LonLat getLonLat() {
      return lonlat;
    }

    public NodeEntry(int nid, LonLat lonlat, int t) {
      this.setNodeID(nid);
      this.lonlat = lonlat;
      this.t = t;
      this.timestamp = null;
      this.setStorageStatus(0);
    }

    public NodeEntry(int nid, LonLat lonlat, DateTime timestamp) {
      this.setNodeID(nid);
      this.lonlat = lonlat;
      this.t = 0;
      this.timestamp = timestamp;
      this.setStorageStatus(0);
    }





    public void print() {
      System.out.println("nid = "+getNodeID()+
          ", lon = "+lonlat.getLon()+
          ", lat = "+lonlat.getLat()+
          ", time = "+t);
    }

    public double getDistance() {
      return distance;
    }

    public DateTime getDateTime() {
      return timestamp;
    }

    public void setDistance(double distance) {
      this.distance = distance;
    }

    public void setNodeID(int nid) {
      this.nid = nid;
    }

    public int getNodeID() {
      return nid;
    }

    public double getStorageStatus() {
      return storageStatus;
    }

    public void setStorageStatus(double storageStatus) {
      this.storageStatus = storageStatus;
    }

}
