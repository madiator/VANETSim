
package edu.usc.anrg.vanetsim.support;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import edu.usc.anrg.vanetsim.manager.UserManager;

/**
 * A class with lots of methods to perform various
 * database transactions with mysql.
 * Not used anymore with the project, but left as is
 * because some of the code here could be reused later.
 * @author Maheswaran Sathiamoorthy
 */

// Sorry for bad formatting here!!
public class SQLHandler {
	public final int BEIJINGLOCALSQL = 1;
	public final int CHICAGOLOCALSQL = 2;
	public final int BEIJINGREMOTESQL = 3;
	public final int VANETREMOTESQL = 4;
	public final int CHICAGOWEEKLOCALSQL = 5;
	public final int BEIJINGROCKYSQL = 6;

  public Statement stmt;
  public ResultSet rs;
  public Connection con;
  public int db;
  public String NODESTOUSEDB;
  public String COLUMNNAME;
  public String DBNAME;

    public int initialize(int db){
        this.db = db;
        String username=null;
        String password=null;
        String urlBase = "jdbc:mysql://localhost:3306/";
        String url="";

        if(db==BEIJINGLOCALSQL) { 						//Beijing trace database
            url = urlBase+"beijing_trace09";
            NODESTOUSEDB = "nodestouse_beijing1000";
            COLUMNNAME = "nid";
            DBNAME = "locationFull";
            username = "user";
        }
        else if(db==CHICAGOLOCALSQL) {					//Chicago bus trace database
            url = urlBase+"chicagobus";
            username = "user";
            NODESTOUSEDB = "nodestouse_chicago";
            COLUMNNAME = "nid";
            DBNAME = "location";
        }
        else if(db==CHICAGOWEEKLOCALSQL) {
          url = urlBase+"chicagobusweek";
          COLUMNNAME = "vid";
          username = "user";
          DBNAME = "snapshots4";
        }
        else{
            System.out.println("Wrong database choice");
            System.exit(-1);
        }

        try{
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(url,username,UserManager.getPassword(username));
            stmt = con.createStatement();
            System.out.println("Succeeded in Connecting to the Database");
            return 1;
         }catch(Exception e)
         {
             e.printStackTrace();
             return 0;
         }
    }

    public long getFirstDate()
    {
        long t=0;
        //System.out.println(req);
        try {

            stmt.executeQuery("SELECT * FROM "+DBNAME+" limit 1;");
            rs = stmt.getResultSet();
            if (rs.next()) {
//                 nodeEntry.id = rs.getInt("id");

                  t = rs.getTimestamp("date").getTime();
            }
            rs.close();
        }
        catch(Exception e)    {
            e.printStackTrace();
        }
        return t;
    }

    public int storeSimplifiedNodeEntry(int nid, int count, double x, double y, double z,  String time, double speed)
    {
        int succ = 0;
        try{

            stmt.executeUpdate("INSERT INTO simpleLocation(nid,t,x,y,z,time,speed) VALUES("+nid+"," +
                    count+","+x+","+y+","+z+","+
                    "str_to_date('"+time+"','%Y-%m-%d %H:%i:%s'),"+speed+");");


            succ = 1;

        }catch(SQLException e)
        {
            e.printStackTrace();
        }
        return succ;
    }

    public void storeUncodedStorage(int n, int file, int start, int end, int data, int nodeID)
    {
        try
        {
            stmt.executeUpdate("INSERT INTO uncodedStorage VALUES("+n+"," +
                    ""+file+"," +
                    ""+start+"," +
                    ""+end+"," +
                    ""+data+"," +
                    ""+nodeID+");");

        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void deleteUncodedStorage() {
        try {
            stmt.executeUpdate("DELETE FROM uncodedStorage;");
        }catch(Exception e) {
            e.printStackTrace();
        }
    }


    public void storeCodedStorage(int node, int file, int chunk, int start, int end, double data, int nodeID)
    {
         try
        {
            stmt.executeUpdate("INSERT INTO codedStorage VALUES("+node+"," +
                    ""+file+"," +
                    ""+chunk+","+
                    ""+start+"," +
                    ""+end+"," +
                    ""+data+"," +
                    ""+nodeID+");");

        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void deleteCodedStorage() {
        try {
            stmt.executeUpdate("DELETE FROM codedStorage;");
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public List<CodedChunk> getCodedChunk(int nodeID, int file)
    {
        List codedChunkList = new ArrayList();

        int nid, chunk, start, end;
        int nodeNumCheck;
        double data;
        try{
            String qry = "SELECT * FROM codedStorage WHERE nid="+nodeID+" and file="+file+";";
            stmt.executeQuery(qry);
            rs = stmt.getResultSet();
            while(rs.next())
            {
                nodeNumCheck = rs.getInt("node");
                nid = rs.getInt("nid");
                //file = rs.getInt("file");
                chunk = rs.getInt("chunk");
                start = rs.getInt("start");
                end = rs.getInt("end");
                data = rs.getDouble("data");
                if(nid!=nodeID)
                    System.exit(-1);

                CodedChunk c = new CodedChunk(file, chunk, start, end, data, nid);
                codedChunkList.add(c);
            }
            rs.close();
        }catch(Exception e) {
            e.printStackTrace();
        }
        return codedChunkList;
    }

    public List<CodedChunk> getCodedChunk(int nodeID)
    {
        List codedChunkList = new ArrayList();

        int nid, chunk, start, end, file;
        int nodeNumCheck;
        Double data;
        try{
            String qry = "SELECT * FROM codedStorage WHERE nid="+nodeID+";";
            stmt.executeQuery(qry);
            rs = stmt.getResultSet();
            while(rs.next())
            {
                nodeNumCheck = rs.getInt("node");
                nid = rs.getInt("nid");
                file = rs.getInt("file");
                chunk = rs.getInt("chunk");
                start = rs.getInt("start");
                end = rs.getInt("end");
                data = rs.getDouble("data");
                if(nid!=nodeID)
                    System.exit(-1);

                CodedChunk c = new CodedChunk(file, chunk, start, end, data, nid);
                codedChunkList.add(c);
            }
            rs.close();
        }catch(Exception e) {
            e.printStackTrace();
        }
        return codedChunkList;
    }
    public DateTime getFirstDateTime() {
      DateTime t = null;
      try {
        stmt.executeQuery("SELECT min(date) AS date FROM "+DBNAME);
        rs = stmt.getResultSet();
        if(rs.next()) {
          t = new DateTime(rs.getTimestamp("date"));
        }
        rs.close();
      }catch(Exception e) {
        e.printStackTrace();
      }
      return t;
    }

    public DateTime getLastDateTime() {
      DateTime t = null;
      try {
        stmt.executeQuery("SELECT max(date) AS date FROM "+DBNAME);
        rs = stmt.getResultSet();
        if(rs.next()) {
          t = new DateTime(rs.getTimestamp("date"));
        }
        rs.close();
      }catch(Exception e) {
        e.printStackTrace();
      }
      return t;
    }


    public List<NodeEntry> getNodeEntryListByRow(int rowIndex, int numRows) {
    	  int id;
        int nid = 0;
        double lat = 0;
        double lon = 0;
        Timestamp date = null;
        List<NodeEntry> nodeEntryList = new ArrayList<NodeEntry>();

    	try{
            /*String qry = "SELECT "+COLUMNNAME+", format(latitude,20)" +
            		", format(longitude, 20), tmstmp AS "+COLUMNNAME+",latitude" +
            				", longitude, tmstmp FROM "+DBNAME+" limit "+rowIndex+",1"; */
    	      String qry = "SELECT "+COLUMNNAME
    	        +", format(latitude, 20) AS latitude, " +
    	        		"format(longitude, 20) AS longitude, " +
    	        		"date" +
    	      		" FROM "+DBNAME+" limit "+rowIndex+","+numRows;
    	      //System.out.println(qry);
            stmt.executeQuery(qry);
            rs = stmt.getResultSet();
            while(rs.next())
            {
                nid = rs.getInt(COLUMNNAME);
                String tmps = rs.getString("latitude");
                //System.out.println(tmps);
                lat = Double.parseDouble(tmps);
                lon = Double.parseDouble(rs.getString("longitude"));
                //System.out.println("lat = "+lat+"  lon = "+lon);
                date = rs.getTimestamp("date");
                NodeEntry nodeEntry = new NodeEntry(nid, new LonLat(lon, lat), new DateTime(date));
                nodeEntryList.add(nodeEntry);
            }
            rs.close();
        }catch(Exception e) {
            e.printStackTrace();
        }

    	return nodeEntryList;
    }

  public int getNumRows() {
    int count = 0;
    try {
      String qry = "SELECT count(*) as count from "+DBNAME+";";
      stmt.executeQuery(qry);
      rs = stmt.getResultSet();
      if(rs.next()) {
        count = rs.getInt("count");
      }
      rs.close();
    }catch(Exception e) {
      e.printStackTrace();
    }
    return count;
  }

  public List<Integer> getNodes() {
    List<Integer> allNodes = new ArrayList<Integer>();
    try {
      String qry = "SELECT nid from "+NODESTOUSEDB+";";
      stmt.executeQuery(qry);
      rs = stmt.getResultSet();
      while(rs.next()) {
        allNodes.add(rs.getInt("nid"));
      }
      rs.close();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    return allNodes;
  }

}
