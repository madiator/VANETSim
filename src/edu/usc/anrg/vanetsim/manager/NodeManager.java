package edu.usc.anrg.vanetsim.manager;
import java.util.ArrayList;
import java.util.List;

import edu.usc.anrg.vanetsim.Application;
import edu.usc.anrg.vanetsim.support.Configuration;
import edu.usc.anrg.vanetsim.support.NodeEntry;
import edu.usc.anrg.vanetsim.utils.Utils;
/**
 * Manage the set of nodes in the system.
 * When initializing, it will try to connect to the database
 * and download a list of nodes (by using the DBManager instance).
 * @author Maheswaran Sathiamoorthy
 */
public class NodeManager {
  private final List<Integer> nodeList;
  private final int[] nodeIDArray;
  private final int numNodes;
  private int nodeIndex = 0;
  private final DBManager dbManager;

  public NodeManager(Configuration config) {
    System.out.println("* Initializing the Node Manager...");
    dbManager = Application.getDBManager();
    System.out.println("* * Getting a list of nodes..");
    nodeList = dbManager.getAllNodesList();
    numNodes = nodeList.size();
    System.out.println("* * Loaded "+numNodes+" nodes.\n* Done initializing the Node Manager");
    nodeIDArray = new int[numNodes];
    for(int i = 0; i < numNodes; i++)
      nodeIDArray[i] = nodeList.get(i);
  }

  /**
   * Initialize a dummy node manager if you would like.
   * @param numNodes
   */
  public NodeManager(int numNodes) {
    nodeList = new ArrayList<Integer>();
    dbManager = null;
    this.numNodes = numNodes;
    for(int i = 0; i < numNodes; i++)
      nodeList.add(i);
    nodeIDArray = new int[numNodes];
    for(int i = 0; i < numNodes; i++)
      nodeIDArray[i] = nodeList.get(i);
  }

  public int getNodeID(int index) {
    return nodeIDArray[index];
  }

  public int getRandomNodeID() {
    return nodeIDArray[getRandomNodeIndex()];
  }

  public int getRandomNodeIndex() {
    //Math.random returns >=0 and <1 so we are good here
    int randIndex = Utils.randomNumber(numNodes);
    return randIndex;
  }

  public int getNewNodeIndex()  {
    int nodeToReturn = nodeIndex++;
    nodeIndex = nodeIndex % numNodes;
    return nodeToReturn;
  }

	public int getNumNodes() {
		return numNodes;
	}

	public int getNodeIndex(int nodeID) {
	  return nodeList.indexOf(nodeID);
	}

	public int getNodeIndex(NodeEntry node) {
    return getNodeIndex(node.getNodeID());
  }

	public int[] getNodeArray() {
	  return nodeIDArray;
	}
}