/**
 *
 */
package edu.usc.anrg.vanetsim.support;

/**
 * A class to store the contact information
 * between nodes.
 * @author Maheswaran Sathiamoorthy
 *
 */
public class ContactEpisode {
  double startTime;
  private double startDistance;
  double endTime;
  private double endDistance;
  NodeEntry thisNode;
  NodeEntry neighNode;
  public ContactEpisode() {

  }
  public void setStartEndTimes(double s, double t) {
    this.startTime = s;
    this.endTime = t;
  }
  public void setStartEndDistances(double s, double t) {
    this.setStartDistance(s);
    this.setEndDistance(t);
  }
  ContactEpisode(double s, double t) {
    setStartEndTimes(s, t);
  }
  public void setNeighbor(NodeEntry neighNode) {
    this.neighNode = neighNode;
  }
  public double getStartTime() {
    return startTime;
  }
  public double getEndTime() {
    return endTime;
  }
  public double getContactDuration() {
    return (endTime - startTime);
  }
  public NodeEntry getNeighbor() {
    return neighNode;
  }
  public void setStartDistance(double startDistance) {
    this.startDistance = startDistance;
  }
  public double getStartDistance() {
    return startDistance;
  }
  public void setEndDistance(double endDistance) {
    this.endDistance = endDistance;
  }
  public double getEndDistance() {
    return endDistance;
  }
  public NodeEntry getSink() {
    return thisNode;
  }
  public ContactEpisode(int sink, int dest, int timer, double contact) {
    thisNode = new NodeEntry(sink, null, null);
    neighNode = new NodeEntry(dest, null, null);
    startTime = timer*60;
    endTime = startTime + contact;
  }
}
