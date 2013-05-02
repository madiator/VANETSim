package edu.usc.anrg.vanetsim.manager;
import org.joda.time.DateTime;

import edu.usc.anrg.vanetsim.Application;

/**
 * A class that takes care of issues regarding time,
 * since the dataset intricately depends on it.
 * @author Maheswaran Sathiamoorthy
 *
 */
public class TimeManager {
  private DateTime timer;
  DBManager dbManager;
  private final int interval;
  private int slot;
  private final int maxSlots;
  private final DateTime firstTimer, lastTimer;

  public TimeManager() {
    this.dbManager = Application.getDBManager();
    interval = Application.getConfiguration().getInterval();
    DateTime firstTimerTemp = null, lastTimerTemp = null;
    if(dbManager!=null) {
      firstTimerTemp = dbManager.getFirstDateTime();
      lastTimerTemp = dbManager.getLastDateTime();
    }
    if(firstTimerTemp==null)
      firstTimer = new DateTime();
    else
      firstTimer = firstTimerTemp;

    if(lastTimerTemp == null)
      lastTimer = firstTimer.plusDays(1).minusSeconds(1); //add one day if the database didn't return any value
    else
      lastTimer = lastTimerTemp;

    timer = firstTimer;
    maxSlots = (int) (((double)(lastTimer.getMillis()
        - timer.getMillis()))/((double)(getInterval()*1000))) + 1;

    slot = 0;
    System.out.println("Time Manager done loading. " +
    		"The starting time is " + timer + ",\n" +
    		"and the ending time is "+ lastTimer+".\n"+
    		"There are "+maxSlots+" number of slots");
  }

  public void reset() {
    timer = firstTimer;
    slot = 0;
  }

  public void printTime() {
    System.out.println(timer);
  }

  public void printTime(DateTime t) {
    System.out.println(t);
  }
  public void inc() {
    timer = timer.plusSeconds(getInterval());
    slot++;
  }

  public DateTime getThisDateTime() {
    return timer;
  }

  public DateTime getNextDateTime() {
    return timer.plusSeconds(getInterval());
  }

  public boolean hasNext() {
    return (slot < maxSlots);
  }

  public int getTimeSlot() {
    return slot;
  }

  public DateTime getFirstDateTime() {
    return firstTimer;
  }

  public DateTime getLastDateTime() {
    return lastTimer;
  }

  public String getString() {
    return timer.toString();
  }

  public int getSeconds() {
    return slot*getInterval();
  }

  public int getNextSeconds() {
    return (slot + 1)*getInterval();
  }

  public int getNextNextSeconds() {
    return (slot + 2)*getInterval();
  }

  public long getCurrentMillis() {
    return new DateTime().getMillis();
  }

  public int getMaxSlots() {
    return maxSlots;
  }

  public int getInterval() {
    return interval;
  }
}
