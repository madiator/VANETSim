package edu.usc.anrg.vanetsim.manager;

import java.util.HashMap;
import java.util.List;

import edu.usc.anrg.vanetsim.support.ContactEpisode;
import edu.usc.anrg.vanetsim.support.LonLat;
import edu.usc.anrg.vanetsim.support.NodeEntry;
import edu.usc.anrg.vanetsim.support.LonLat.CartesianPosition;

/**
 * An abstract class that defines how a location manager should
 * look like.
 * @author Maheswaran Sathiamoorthy
 *
 */
public abstract class LocationManager {

  protected double range; //in meters
  private final double bigRange = 1000;
  double earthRadius; //in meters

  public abstract List<NodeEntry> getNeighbors(NodeEntry node, TimeManager t);
  public abstract List<NodeEntry> getNeighbors(NodeEntry node, double newRange,
      TimeManager t);

  public abstract NodeEntry getNodeEntry(int nodeID, TimeManager t);
  public abstract List<NodeEntry> getNodeEntriesByTimeAsList(TimeManager t);
  public abstract List<NodeEntry> getNodeEntriesByTimeAsList(int t);
  public abstract HashMap<Integer, NodeEntry> getNodeEntriesByTimeMap(int t);
  public abstract List<NodeEntry> getNodeEntriesByNodeID(int nodeID);

  /**
   *
   * @param l1 LonLat of point 1
   * @param l2 LonLat of pointn 2
   * @return distance in meters
   */
  public double getDistance(LonLat l1, LonLat l2) {
    double lat1 = l1.getLat();
    double lon1 = l1.getLon();
    double lat2 = l2.getLat();
    double lon2 = l2.getLon();

    double lat_diff = Math.toRadians(lat2 - lat1);
    double long_diff = Math.toRadians(lon2 - lon1);
    double a = Math.pow(Math.sin(lat_diff/2),2)
        + Math.cos(Math.toRadians(lat1))
          *Math.cos(Math.toRadians(lat2))
          *Math.pow(Math.sin(long_diff/2),2);
    double c = 2*Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    double d = earthRadius*c;
    return d;
  }

  /** Get a list of contact episodes of node at a particular time.
   * @param node the NodeEntry for the node at current time
   * @param nodeNext the NodeEntry for the node at the next time
   * @param timer the timer
   * @return list of contact episodes
   */
  public abstract List<ContactEpisode> getContactEpisodes(NodeEntry node,
      NodeEntry nodeNext, TimeManager timer);
  public abstract ContactEpisode getContactEpisode(NodeEntry sinkNodeCurrent,
      NodeEntry sinkNodeNext,
      NodeEntry destNodeCurrent,
      NodeEntry destNodeNext,
      int tCurrent, int tNext);

  /**
   * Returns the intersection time of two nodes in units of time.
   * For node 1, the initial and starting positions are denoted by p1 and p2
   * For node 2, they are q1 and q2
   * The time to go from p1 to p2 is 1 unit of time. The unit does not matter
   * since we are only interested in the fraction of the time unit for which
   * they intersect.
   * @param p1 Initial position of node 1 (t = 0)
   * @param p2 Final position of node 1 (t = 1)
   * @param q1 Initial position of node 2 (t = 0)
   * @param q2 Final position of node 2 (t = 1)
   * @param r The wireless radio range
   * @param contactEpisode Store the start and end contact times
   */
  public ContactEpisode intersectionTime(
                                CartesianPosition p1, CartesianPosition p2,
                                CartesianPosition q1, CartesianPosition q2,
                                double r,
                                int t1, int t2) {
    // r must be in meters
    //Trying to solve: (ax*t + bx)^2 + .. <= r^2
    double ax = (q2.x-q1.x)-(p2.x-p1.x);
    double bx = q1.x-p1.x;
    double ay = (q2.y - q1.y)-(p2.y-p1.y);
    double by = q1.y-p1.y;
    double az = (q2.z - q1.z)-(p2.z-p1.z);
    double bz = q1.z-p1.z;

    double a = ax*ax + ay*ay + az*az;
    double b = 2*(ax*bx+ay*by+az*bz);
    double c = bx*bx + by*by +bz*bz - r*r;
    double detsq = b*b - 4*a*c;

    double startTime = 0;
    double endTime = 0;
    double root11=-1, root12=-1, root1=-1, root2=-1;
    if(detsq>=0)
    {
      if(a!=0)
      {
        double det = Math.sqrt(detsq);
        root11 = (-b - det) / (2 * a);
        root12 = (-b + det) / (2 * a);
        root1 = Math.min(root11, root12);
        root2 = Math.max(root11, root12);
      } else if(b!=0){
        root1 = 0;
        root2 = -c/b;
      }
      else {
        // so a = 0, b = 0
        root1 = 0;
        root2 = 0;
        if(c<=0)
          root2 = 1;
      }

    } else if(a < 0) {
      root1 = 0;
      root2 = 1;
    }
    if ((root1 <= 1) && (root2 >= 0)) {
      startTime = Math.max(0, root1);
      endTime = Math.min(1,root2);
    }
    double startDistance = Math.sqrt(
          a*startTime*startTime + b*startTime + c + r*r
        );
    double endDistance = Math.sqrt(a*endTime*endTime + b*endTime + c + r*r);
    ContactEpisode contactEpisode = new ContactEpisode();
    contactEpisode.setStartEndTimes(
        t1 + (t2 - t1)*startTime, t1 + (t2 - t1)*endTime);
    contactEpisode.setStartEndDistances(startDistance, endDistance);
    return contactEpisode;
  }

  public double getBigRange() {
    return bigRange;
  }


}
