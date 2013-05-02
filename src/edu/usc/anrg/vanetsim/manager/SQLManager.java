/**
 *
 */
package edu.usc.anrg.vanetsim.manager;

import java.util.HashMap;
import java.util.List;

import org.joda.time.DateTime;

import edu.usc.anrg.vanetsim.support.Configuration;
import edu.usc.anrg.vanetsim.support.ContactEpisode;
import edu.usc.anrg.vanetsim.support.NodeEntry;

/**
 * Unfinished. Currently, I have implemented only for MongoDB
 * @author Maheswaran Sathiamoorthy
 *
 */
public class SQLManager  extends DBManager {
  public SQLManager(Configuration cofig) {

  }

  @Override
  public List<Integer> getAllNodesList() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DateTime getFirstDateTime() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DateTime getLastDateTime() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<NodeEntry> getNeighbors(NodeEntry node, double range, int t1,
      int t2) {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public NodeEntry getNodeEntry(int nid, int t1, int t2) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NodeEntry getOneNodeEntry(int nid) {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public NodeEntry getNodeEntry(int nid, int t) {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see edu.usc.anrg.vsim.discrete.manager.DBManager#getNeighbors(edu.usc.anrg.vsim.discrete.support.NodeEntry, double, int)
   */
  @Override
  public List<NodeEntry> getNeighbors(NodeEntry node, double range, int t) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<NodeEntry> getNodeEntries(int nid) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<NodeEntry> getNodeEntriesByTimeAsList(int t1, int t2) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public HashMap<Integer, NodeEntry> getNodeEntriesByTimeAsHashMap(int t,
      HashMap<Integer, NodeEntry> locations) {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see edu.usc.anrg.vsim.discrete.manager.DBManager#getNodeEntriesByTimeAsList(int)
   */
  @Override
  public List<NodeEntry> getNodeEntriesByTimeAsList(int t) {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see edu.usc.anrg.vsim.discrete.manager.DBManager#getEasyContactEpisodes(int)
   */
  @Override
  public List<ContactEpisode> getEasyContactEpisodes(int timer) {
    // TODO Auto-generated method stub
    return null;
  }


}
