package edu.usc.anrg.vanetsim.utils;

import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

import edu.usc.anrg.vanetsim.support.NodeEntry;
import edu.usc.anrg.vanetsim.support.SQLHandler;

public class BeijingExporter {
	private static SQLHandler sqlHandler;

	public static void main(String args[]) {

		try {
			sqlHandler = new SQLHandler();
			int dbToUse = sqlHandler.BEIJINGREMOTESQL;
	      int noError = sqlHandler.initialize(dbToUse);
	      if (noError != 1) {
	        System.out.println("Error in establishing database");
	        System.exit(-1);
	      }
			Mongo m = new Mongo();
			DB db = m.getDB( "beijingtest" );
			DBCollection coll = db.getCollection("locations");

			// First count how many entries are there in the table.
			int rowToStart = 0;
			int numRows = sqlHandler.getNumRows();
			int numRowsPerFetch = 200;
			System.out.println(numRows);
			NodeEntry nodeEntry;
			int count = 0;
			// First store the starting time
			DateTime startingTime = sqlHandler.getFirstDateTime();
			DateTime endingTime = sqlHandler.getLastDateTime();
			BasicDBObject tdoc = new BasicDBObject();
			tdoc.put("startingtime", startingTime.toDate());
			tdoc.put("endingtime", endingTime.toDate());
			tdoc.put("startingseconds", 0);
			int endingSeconds = (int)(endingTime.getMillis() - startingTime.getMillis())/1000;
			tdoc.put("startingseconds", endingSeconds);
			coll.insert(tdoc);

			for(int i=rowToStart;i<numRows;i+=numRowsPerFetch) {
				List<NodeEntry> nodeEntryList = sqlHandler.getNodeEntryListByRow(i, numRowsPerFetch);
				Iterator<NodeEntry> itr = nodeEntryList.iterator();

				while(itr.hasNext()) {
				  nodeEntry = itr.next();
				  if(nodeEntry.getLat()!=0) {
    				BasicDBObject doc = new BasicDBObject();
    				BasicDBObject loc = new BasicDBObject();
    				int extraSeconds = (int)(nodeEntry.getDateTime().getMillis() - startingTime.getMillis())/1000;
    				doc.put("nid", nodeEntry.getNodeID());
    				doc.put("t", extraSeconds);
    				//doc.put("timestamp", nodeEntry.getDateTime().toDate());
    				loc.put("lon", nodeEntry.getLon());
    				loc.put("lat", nodeEntry.getLat());
    				doc.put("loc", loc);
    				coll.insert(doc);
				  }
				}

				count+=numRowsPerFetch;
				if(count%1000==0)
				  System.out.println(count);
			}



			// need to read each entry of the table from mysql and insert
			// into mongodb, so I can make use of geospatial indexing and
			// fast reads from mongodb

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MongoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


}