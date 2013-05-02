package edu.usc.anrg.vanetsim.manager;

/** Create a class called UserManager
 * similar to this. It will be used by
 * Configuration
 * @author Maheswaran Sathiamoorthy
 */
public class UserManagerDummy {
  public static String getPassword(String username) {
    if("yourusername".equals(username)) {
      return "yourpassword";
    }
    else {
      return "unknown";
    }
  }
}
