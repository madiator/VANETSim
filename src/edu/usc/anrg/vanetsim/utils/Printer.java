/**
 *
 */
package edu.usc.anrg.vanetsim.utils;

/**
 * @author Maheswaran Sathiamoorthy
 *
 */
public class Printer {
  static String str;
  public static Printer start() {
    str = "";
    Printer p = new Printer();
    return p;
  }
  public Printer add(Object x) {
    str = str + x;
    return this;
  }
  public Printer newline() {
    str = str + "\n";
    return this;
  }
  public Printer newP() {
    str = "";
    return this;
  }
  public Printer tab() {
    str = str + "\t";
    return this;
  }
  public String getString() {
    return str;
  }
  public void print() {
    System.out.print(str);
  }
  public void println() {
    System.out.println(str);
  }
}
