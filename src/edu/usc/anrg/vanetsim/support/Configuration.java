package edu.usc.anrg.vanetsim.support;

import edu.usc.anrg.vanetsim.manager.NodeManager;

/**
 * A class to store all the configuration variables
 * required by the simulator.
 * @author Maheswaran Sathiamoorthy
 *
 */
/*
 * Ideally this file should be reading the configuration values from
 * an XML file.
 */
public class Configuration {
	//private DBManager dbManager;
	private int numNodes;
	private int numFiles, fileSize, capacity, kCoding, storageRedundancy;
	private int blockSize;
	private final double range = 100; //in meters
	private int interval; //the interval for simulation in seconds
	private String hostName = null;
	private int port = 0;
	private String dbName, collName, nodeCollName="nodes";
	private NodeManager nodeManager;
	public final String dbType;
	public static final double earthRadius = 6378137; //in meters
	private boolean isCodedStorage;
  private boolean uncodedDistributed;

	public Configuration(String dbType) {
		this.dbType = dbType;
	}

  public void setNumNodes(int numNodes) {
    this.numNodes = numNodes;
  }
  public int getNumNodes() {
    return numNodes;
  }
  public void setNumFiles(int numFiles) {
    this.numFiles = numFiles;
  }
  public int getNumFiles() {
    return numFiles;
  }
  public void setFileSize(int fileSize) {
    this.fileSize = fileSize;
  }
  public int getFileSize() {
    return fileSize;
  }
  public void setCapacity(int capacity) {
    this.capacity = capacity;
  }
  public int getCapacity() {
    return capacity;
  }

  public double getRange() {
    return range;
  }

  public void setInterval(int interval) {
    this.interval = interval;
  }

  public int getInterval() {
    return interval;
  }

  public String getDbName() {
    return dbName;
  }

  public void setDbName(String dbName) {
    if(dbName.startsWith("beijing"))
      setInterval(60);
    else
      setInterval(30);

    this.dbName = dbName;
  }

  public String getCollName() {
    return collName;
  }

  public void setCollName(String collName) {
    this.collName = collName;
  }

  public String getHostName() {
    return hostName;
  }

  public void setHostName(String hostName) {
    this.hostName = hostName;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public void setNodeManager(NodeManager nodeManager) {
    this.nodeManager = nodeManager;
  }

  public NodeManager getNodeManager() {
    return nodeManager;
  }

  public int getStorageRedundancy() {
    return storageRedundancy;
  }

  public void setStorageRedundancy(int storageRedundancy) {
    this.storageRedundancy = storageRedundancy;
  }

  public double getEarthRadius() {
    return earthRadius;
  }

  public int getBlockSize() {
    return blockSize;
  }

  public void setBlockSize(int blockSize) {
    this.blockSize = blockSize;
  }

  public boolean isCodedStorage() {
    return isCodedStorage;
  }

  public void setCodedStorage(boolean isCodedStorage) {
    this.isCodedStorage = isCodedStorage;
  }

  public int getkCoding() {
    return kCoding;
  }

  public void setkCoding(int kCoding) {
    this.kCoding = kCoding;
  }

  public boolean isUncodedDistributed() {
    return uncodedDistributed;
  }

  public void setUncodedDistributed(boolean uncodedDistributed) {
    this.uncodedDistributed = uncodedDistributed;
  }

  public String getNodeCollName() {
    return nodeCollName;
  }

  public void setNodeCollName(String nodeCollName) {
    this.nodeCollName = nodeCollName;
  }

}
