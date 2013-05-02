/**
 *
 */
package edu.usc.anrg.vanetsim.manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import edu.usc.anrg.vanetsim.support.Configuration;
import edu.usc.anrg.vanetsim.support.NodeEntry;
import edu.usc.anrg.vanetsim.utils.Printer;
import edu.usc.anrg.vanetsim.utils.Utils;

/**
 * The class that manages the storage of all the files
 * in the system.
 * @author Maheswaran Sathiamoorthy
 *
 */
public class StorageManager {
  public final static boolean CODED = true;
  public final static boolean UNCODED = false; //when in doubt, choose this.

  private final int commonFileSize, capacity, numFiles, storageRedundancy;
  private final int commonBlockSize;
  private final int nCoding, kCoding;
  private final int numNodes;
  private final boolean uncodedDistributed;

  NodeManager nodeManager;
  NodeStorageManager[] nodeStorageManager;

  boolean isCodedStorage;
  //private final int storageRedundancy;

  public StorageManager(Configuration config) {

    this.kCoding = config.getkCoding();
    storageRedundancy = config.getStorageRedundancy();
    this.isCodedStorage = config.isCodedStorage();
    this.commonBlockSize = config.getBlockSize();

    if(isCodedStorage) {
      this.nCoding = kCoding*storageRedundancy;
      this.commonFileSize =
          nCoding; //now file size is in blocks
      // this must be equal to nCoding in case of coded storage)
      this.uncodedDistributed = false;
    }
    else {
      this.nCoding = kCoding;
      this.commonFileSize =
          config.getFileSize()/commonBlockSize; //now file size is in blocks
      // this must be equal to kCoding for uncoded storage
      this.uncodedDistributed = config.isUncodedDistributed();
    }
    this.capacity = config.getCapacity()/commonBlockSize;
    this.numFiles = config.getNumFiles();
    this.nodeManager = config.getNodeManager();
    this.numNodes = nodeManager.getNumNodes();



    nodeStorageManager = new NodeStorageManager[numNodes];
    for(int nodeIndex = 0; nodeIndex < numNodes; nodeIndex++) {
      nodeStorageManager[nodeIndex] = new NodeStorageManager(capacity);
    }
  }

  public boolean hasFile(int nodeIndex, int fileIndex) {
    return nodeStorageManager[nodeIndex].hasFile(fileIndex);
  }

  public boolean hasFullFile(int nodeIndex, int fileIndex) {
    return nodeStorageManager[nodeIndex].hasFullFile(fileIndex);
  }

  public void initializeStorage() {
    if(isCodedStorage)
      initializeCodedStorage();
    else if(uncodedDistributed)
      initializeUncodedStorageDistributed();
    else
      initializeUncodedStorage();
  }

  public void initializeCodedStorage() {
    for(int file = 0; file < numFiles; file++) {
      for(int blockIndex = 0; blockIndex < nCoding; blockIndex++) {
        boolean isStored = false;
        while(!isStored) {
          int nodeIndexToStore = nodeManager.getNewNodeIndex();
          while(
                (nodeStorageManager[nodeIndexToStore].isFull) ||
                (nodeStorageManager[nodeIndexToStore].hasFullBlock(
                    file, blockIndex)
                )
               ) {
            nodeIndexToStore = nodeManager.getNewNodeIndex();
          }
          isStored =
              nodeStorageManager[nodeIndexToStore].storeBlock(
                  file, blockIndex, commonFileSize
                  );
        }
      }



    }
  }

  public void initializeUncodedStorage() {
    for(int storageAttempt = 1; storageAttempt <= storageRedundancy;
        storageAttempt++) {
      for(int file = 0; file < numFiles; file++) {
        boolean isStored = false;
        while(!isStored) {
          int nodeIndexToStore = nodeManager.getRandomNodeIndex();
          while((nodeStorageManager[nodeIndexToStore].isFull) ||
              (nodeStorageManager[nodeIndexToStore].hasFullFile(file))) {
            nodeIndexToStore = nodeManager.getRandomNodeIndex();
          }
          isStored =
              nodeStorageManager[nodeIndexToStore].storeFile(
                  file, commonFileSize);
        }
      }
    }
  }

  public void initializeUncodedStorageDistributed() {
    for(int storageAttempt = 1;
        storageAttempt <= storageRedundancy; storageAttempt++) {
      for(int file = 0; file < numFiles; file++) {
        // Assume the file is split into kCoding number of blocks;
        for(int blockIndex = 0; blockIndex < kCoding; blockIndex++) {
          boolean isStored = false;
          while(!isStored) {
            int nodeIndexToStore = nodeManager.getNewNodeIndex();
            while(
                  (nodeStorageManager[nodeIndexToStore].isFull) ||
                  (nodeStorageManager[nodeIndexToStore].hasFullBlock(
                      file, blockIndex)
                  )
                 ) {
              nodeIndexToStore = nodeManager.getNewNodeIndex();
            }
            isStored =
                nodeStorageManager[nodeIndexToStore].storeBlock(
                    file, blockIndex, commonFileSize
                    );
          }
        }
      }
    }
  }

  /**
   * Copy a file identified by fileIndex from the source to destination.
   * The transfer could fail if the source does not have the file or
   * if the destination node has fully utilized its storage.
   *
   * @param downloaderNode the node which wants to download (destination)
   * @param uploaderNode the node which has the file (uploader)
   * @param file the file to be downloaded
   * @return true if the file is now complete.
   *
   */
  public boolean copyFile(NodeEntry downloaderNode,
                          NodeEntry uploaderNode,
                          int file,
                          int maxBytes) {

    int uploaderNodeIndex =
      nodeManager.getNodeIndex(uploaderNode.getNodeID());
    int downloaderNodeIndex =
      nodeManager.getNodeIndex(downloaderNode.getNodeID());
    NodeStorageManager uM = nodeStorageManager[uploaderNodeIndex];
    NodeStorageManager dM = nodeStorageManager[downloaderNodeIndex];

    // Few cases:
    // dM has full file - return true
    // uM has full file, dM has no file - try to store new file of size
    //   min(fileSize, maxBytes)
    // uM has partial file, dM has no or partial file - try to store from uM
    // uM has no file - return false

    if(dM.hasFullFile(file))
      return true;
    else if(!uM.hasFile(file))
      return false;
    else if((uM.hasFullFile(file))&&(!dM.hasFile(file))) {
      int fileSize = uM.fileManager.get(file).thisFileSize;
      fileSize = fileSize>maxBytes?maxBytes:fileSize;
      dM.storeFile(file, fileSize);
      return dM.hasFullFile(file);
    }
    else {
      File dFileObj = null;
      File uFileObj = uM.fileManager.get(file);
      if(dM.hasFile(file)) {
        dFileObj = dM.fileManager.get(file);
      } else {
        dFileObj = new File(0, commonFileSize);
      }
      dM.extraStorageUsed(dFileObj.copyFile(uFileObj, maxBytes));
      dM.fileManager.put(file, dFileObj);
      return dFileObj.isComplete();
    }
  }

  public void deleteFile(NodeEntry node, int fileID, int numBytes) {
    int nodeIndex =
        nodeManager.getNodeIndex(node.getNodeID());
    nodeStorageManager[nodeIndex].deleteFile(fileID, numBytes);
  }

  /**
   * Method to find out how many nodes carry the particular file
   * TODO: Should I count only full files?
   * @param fileIndex
   * @return
   *
   */
  public int getNodeCount(int fileIndex) {
    int count = 0;
    for(int nodeIndex=0; nodeIndex < numNodes; nodeIndex++) {
      if(hasFullFile(nodeIndex, fileIndex))
        count++;
    }
    return count;
  }

  /** Method to get the node count for all files
   *
   * @param fileCounts array which will contain the file counts.
   */
  public void getNodeCount(int[] fileCounts) {
    for(int fileIndex = 0; fileIndex < fileCounts.length; fileIndex++)
      fileCounts[fileIndex] = getNodeCount(fileIndex);
  }

  public double getStorageStatus(NodeEntry nodeEntry, int fileIndex) {
    int nodeIndex = nodeManager.getNodeIndex(nodeEntry.getNodeID());
    return getStorageStatus(nodeIndex, fileIndex);
  }

  public double getStorageStatus(int nodeIndex, int fileIndex) {
    if(!nodeStorageManager[nodeIndex].hasFile(fileIndex)) {
      return 0;
    }
    File file = nodeStorageManager[nodeIndex].fileManager.get(fileIndex);
    //return file.getFileCompletionRatio();
    return file.getThisFileSize();

  }

  /**
   * A class that manages the storage for a single node.
   * The fileManager is used to manage the files of this
   * node. It is a HashMap, which maps a file to its corresponding
   * File object.
   *
   * @author Maheswaran Sathiamoorthy
   *
   */
  public class NodeStorageManager {
    private boolean isFull;
    int totalStorageUsed;
    int thisCapacity;
    HashMap<Integer, File> fileManager;

    //int maxFiles = (int) Math.floor(thisCapacity/commonFileSize);
    protected NodeStorageManager(int cap) {
      isFull = false;
      totalStorageUsed = 0;
      this.thisCapacity = cap;
      fileManager = new HashMap<Integer, File>();
    }

    private void extraStorageUsed(int b) {
      totalStorageUsed +=b;

      if(totalStorageUsed == capacity)
        isFull = true;
    }

    /** Store the given file into the node.
     * @param fileToStore
     * the file to be stored
     * @param fileSize the size of the file to be stored.
     * @return true
     * if storing the file was successful
     * or false if it was not.
     */
    boolean storeFile(int fileToStore, int fileSize) {
      /* If the full file is already present, do nothing.
       * Else, if part of the file exists, complete it to full file.
       * TODO: the above could be problematic.
       * If the file doesn't exist at all, then create a new
       * one and store.
       */
      if(isFull)
        return false;
      if(hasFullFile(fileToStore))
        return false;
      else if(hasFile(fileToStore)) {
        File file = fileManager.get(fileToStore);
        extraStorageUsed(file.getMaxFileSize() - file.getThisFileSize());
        file.fillFile();
        return true;
      } else if(totalStorageUsed + fileSize > capacity) {
        fileSize = capacity - totalStorageUsed;
        File file = new File(fileSize, commonFileSize);
        fileManager.put(fileToStore, file);
        extraStorageUsed(file.getThisFileSize());
        return true;
      } else {
        File file = new File(fileSize, commonFileSize);
        fileManager.put(fileToStore, file);
        extraStorageUsed(file.getThisFileSize());
        return true;
      }
    }
    /**
     * Store the block of a file into a node
     * @param fileToStore
     * @param blockIndex
     * @param fileSize
     * @return
     */
    boolean storeBlock(int fileToStore, int blockIndex, int fileSize) {
      if(isFull)
        return false;
      if(hasFullFile(fileToStore))
        return false;
      else if(hasBlock(fileToStore, blockIndex)) {
        return false; //already has block!, though this should never happen
      } else if (totalStorageUsed >= capacity){
        return false; //no space for one more block!
      } else if (hasFile(fileToStore)){
        // File exists but not the block
        File file = fileManager.get(fileToStore);
        file.putBlock(blockIndex);
        fileManager.put(fileToStore, file);
        extraStorageUsed(1);
        return true;
      } else {
        File file = new File(0, commonFileSize);
        file.putBlock(blockIndex);
        fileManager.put(fileToStore, file);
        extraStorageUsed(1);
        return true;
        // File does not exist, therefore neither the block
      }
    }

    /**
     * Is the file stored in this node partially or completely?
     * @param fileToSearch the file index to search for
     * @return true if it is already stored, false else.
     */
    public boolean hasFile(int fileToSearch) {
      return fileManager.containsKey(fileToSearch);
    }

    public boolean hasFullFile(int fileToSearch) {
      /* First see whether the file exists
       * If it doesn't, return false.
       * If it exists, then get the File object to see
       * whether it contains the complete file.
       */
      if(hasFile(fileToSearch)) {
        return fileManager.get(fileToSearch).isComplete();
      }else {
        return false;
      }
    }

    public boolean hasFullBlock(int fileToSearch, int blockToSearch) {
      return hasBlock(fileToSearch, blockToSearch);
    }

    public boolean hasBlock(int fileToSearch, int blockToSearch) {
      /* First see whether the file exists
       * If it doesn't, return false.
       * If it exists, then get the File object to see
       * whether it contains the complete block.
       */
      if(hasFile(fileToSearch)) {
        return fileManager.get(fileToSearch).hasBlock(blockToSearch);
      }else {
        return false;
      }
    }

    /**
     * Delete a file
     */
    public boolean deleteFile(int fileID, int numBytes) {
      File fileObj = fileManager.get(fileID);
      if(fileObj==null)
        return false;
      if(fileObj.thisFileSize <= numBytes) {
        // Delete whole file
        fileManager.remove(fileID);
        extraStorageUsed(-fileObj.thisFileSize);
        return true;
      } else {
        fileObj.removeBlocks(numBytes);
        extraStorageUsed(-numBytes);
        fileManager.put(fileID, fileObj);
        return true;
      }
    }



  }

  /**
   * File is a class that is used to manage a single file.
   * It contains a HashSet called data, which stores the
   * file. The HashSet just stores the values of the indices
   * starting from 0 till maxFileSize.
   * More efficient implementations can be possible.
   * @author Maheswaran Sathiamoorthy
   *
   */
  class File {
    HashSet<Integer> data;

    int maxFileSize;  // if file size is this, it can be considered full
    int thisFileSize; //current file size.

    File(int fileSize, int maxFileSize) {
      this.thisFileSize = fileSize;
      this.maxFileSize = maxFileSize;
      data = new HashSet<Integer>();
      for(int e = 0; e < fileSize; e++)
        data.add(e);
    }

    void fillFile() {
      for(int e = 0; e < maxFileSize; e++)
        data.add(e);
      thisFileSize = maxFileSize;
    }
    void fillFile(int howMany) {
      int added = 0;
      for(int e = 0; e < maxFileSize; e++) {
        if(data.add(e))
          added++;
        if(added>=howMany)
          break;
      }
    }

    /**
     * Put a block and return true if successful.
     * Failure can happen if blockIndex is illegal
     * or if the block already exists.
     * @param blockIndex
     * @return
     */
    boolean putBlock(int blockIndex) {
      if(blockIndex >= maxFileSize)
        return false;
      if(data.add(blockIndex)) {
        thisFileSize += 1;
        return true;
      }
      else {
        return false;
      }
    }
    void removeBlocks(int numBlocks) {
      int numRemoved = 0;
      for(int e = 0; e < maxFileSize; e++) {
        if((numRemoved < numBlocks) && data.remove(e)) {
          numRemoved++;
        }
      }
      thisFileSize -= numRemoved;
    }

    boolean hasBlock(int blockIndex) {
      return data.contains(blockIndex);
    }

    boolean hasFullBlock(int blockIndex) {
      return hasBlock(blockIndex); //blocks cannot be incomplete
    }

    boolean isComplete() {
      if(isCodedStorage)
        return thisFileSize>=kCoding;
      else
        return thisFileSize==maxFileSize;
    }

    int getThisFileSize() {
      return thisFileSize;
    }

    int getMaxFileSize() {
      return maxFileSize;
    }

    double getFileCompletionRatio() {
      if(isCodedStorage)
        return (thisFileSize/(double)kCoding);
      else
        return (thisFileSize/(double)maxFileSize);
    }


    int copyFile(File srcFile, int maxBytes) {
      /* First check if the srcFile is complete.
       * If it is complete, then no need to look into the arrays.
       */
      if(srcFile.isComplete()) {
        int numBlocksToAdd = 0;
        if(isCodedStorage)
          numBlocksToAdd = Utils.min(kCoding - thisFileSize, maxBytes);
        else
          numBlocksToAdd = Utils.min(maxFileSize - thisFileSize, maxBytes);
        fillFile(numBlocksToAdd);
        thisFileSize += numBlocksToAdd;
        return numBlocksToAdd;
      }

      Integer[] srcDataArray =  new Integer[srcFile.data.size()];
      srcFile.data.toArray(srcDataArray);
      int numAdded = 0;
      for(int e:srcDataArray) {
        if((numAdded < maxBytes) && data.add(e) && (data.size() <= kCoding)) {
        //TODO: Here I am assuming kCoding = file size.
          numAdded++;
        }
      }
      thisFileSize += numAdded;
      return numAdded;
    }
  }

  public boolean isComplete(int nodeIndex, int fileIndex) {
    return
        nodeStorageManager[nodeIndex].fileManager.get(fileIndex).isComplete();
  }

  public void printStorage() {
    for(int nodeIndex = 0; nodeIndex < numNodes; nodeIndex++)
      printStorage(nodeIndex);
    System.out.println("-----");
    Printer.start()
      .add("Total storage used by all nodes = ")
      .add(getTotalStorageUsed())
      .newline()
      .add("==============")
      .println();

  }

  public void printStorage(int nodeIndex) {
    System.out.println("Node Index "+nodeIndex+
        " with ID " + nodeManager.getNodeID(nodeIndex) + ":");
    Set<Integer> fileListArray =
        nodeStorageManager[nodeIndex].fileManager.keySet();
    for(int file:fileListArray) {
      //System.out.println(" Has file "+file+ " | ");
      Printer p = Printer.start().add("* has file ").add(file).add(" | ");
      File fileObj = nodeStorageManager[nodeIndex].fileManager.get(file);
      Integer[] fileDataArray = new Integer[fileObj.data.size()];
      fileObj.data.toArray(fileDataArray);
      for(int fileData:fileDataArray) {
        p.add(fileData).add(" ");
      }
      p.println();
    }
    Printer.start()
      .add("Total used storage = ")
      .add(nodeStorageManager[nodeIndex].totalStorageUsed)
      .add(" of ")
      .add(capacity)
      .add("  available space")
      .println();
    System.out.println("-----");
  }

  public int getTotalStorageUsed() {
    int total = 0;
    for(int nodeIndex = 0; nodeIndex < numNodes; nodeIndex++) {
      total += nodeStorageManager[nodeIndex].totalStorageUsed;
    }
    return total;
  }


}

