package edu.usc.anrg.vanetsim;


import edu.usc.anrg.vanetsim.manager.TimeManager;
import edu.usc.anrg.vanetsim.support.Configuration;

/**
 * A Class to collect metrics.
 * @author Maheswaran Sathiamoorthy
 *
 */
public class MetricCollector {
  private final double[][] storageStatusValues;
  private final boolean[][] isDoneValues;
  private final TimeManager timer;
  private final int maxSlots;

  public MetricCollector(int numTrials) {
    timer = Application.getTimeManager();
    this.maxSlots = timer.getMaxSlots();
    storageStatusValues = new double[maxSlots][numTrials];
    isDoneValues = new boolean[maxSlots][numTrials];
    Configuration config = Application.getConfiguration();
  }


  /**
   * Records the storage status for each trial
   * @param trial The trial at which to record
   * @param status The storage status to record
   */
  public void recordStorageStatus(int trial,  double status) {
    int t = timer.getTimeSlot();
    storageStatusValues[t][trial] = status;
    if(status>=100)
      isDoneValues[t][trial] = true;
    else
      isDoneValues[t][trial] = false;
  }

  /**
   * If implemented, can be used to keep a history of the contacts
   * between pairs of nodes.
   */
  public void storeContacts(int nodeIndex1, int nodeIndex2) {

  }

  /**
   * Show the storage results so far
   */
  public void showStorageResults() {
    for(int i = 0; i < maxSlots; i++) {
      double sum = 0;
      int isDone = 0;
      int len = storageStatusValues[i].length;
      System.out.print(i+", ");
      for(int j = 0; j < len; j++) {
        System.out.print(storageStatusValues[i][j]+", ");
        sum += storageStatusValues[i][j];
        isDone += storageStatusValues[i][j]>=100.0?1:0;
      }
      System.out.println((sum/len)+", "+(double)isDone/len);
    }

  }
}
