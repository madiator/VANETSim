package edu.usc.anrg.vanetsim.support;

/**
 * Useful for storing the longitude and latitude.
 * @author Maheswaran Sathiamoorthy
 *
 */
public class LonLat {
  private double lon;
  private double lat;
  public LonLat() {
    setLon(0);
    setLat(0);
  }
  public LonLat(double lon, double lat) {
    setLon(lon);
    setLat(lat);
  }
  public double getLon() {
    return lon;
  }
  public void setLon(double lon) {
    this.lon = lon;
  }
  public double getLat() {
    return lat;
  }
  public void setLat(double lat) {
    this.lat = lat;
  }

  public boolean equals(LonLat lonlat) {
    if((lon==lonlat.getLon()) && (lat==lonlat.getLat()))
      return true;
    return false;
  }

  public class CartesianPosition {
    public double x, y, z;
    public CartesianPosition(double x, double y, double z) {
      this.x = x;
      this.y = y;
      this.z = z;
    }
  }

  /**
   * Convert the lon, lat to cartesian positions.
   * @return
   */
  public CartesianPosition getCartesianPosition() {
    double R = Configuration.earthRadius; //meters
    double x1 = R*Math.cos(Math.toRadians(lat))*Math.cos(Math.toRadians(lon));
    double y1 = R*Math.cos(Math.toRadians(lat))*Math.sin(Math.toRadians(lon));
    double z1 = R*Math.sin(Math.toRadians(lat));
    return new CartesianPosition(x1, y1, z1);
  }

  /**
   * If a node moves from LonLat l1 at time t1 to LonLat l2 at time t2,
   * then given an intermediate time t between t1 and t2, this method
   * can compute the LonLat at that time. It uses linear interpolation,
   * but it can be improved by noting that the earth's surface is not
   * flat. Could be accurate for small movements, but grossly inaccurate
   * for large movements.
   * @param l1
   * @param t1
   * @param l2
   * @param t2
   * @param t
   * @return
   */
  public static LonLat getIntermediateLonLat(LonLat l1, int t1, LonLat l2, int t2, int t) {
    //if(t1==t2)
    //  return l1; //this shouldn't occur

    double lon = l1.lon + (t - t1)*(l2.lon - l1.lon)/(t2 - t1);
    double lat = l1.lat + (t - t1)*(l2.lat - l1.lat)/(t2 - t1);
    return new LonLat(lon, lat);
    // We could have another implementation that could involve the cartesian positions.
  }
}
