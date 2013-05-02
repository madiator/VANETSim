
package edu.usc.anrg.vanetsim.support;

/**
 * A class to store a coded chunk, required by
 * {@link SQLHandler}, and note that {@link SQLHandler}
 * is not required, but has a set of commands to
 * interface with mysql.
 * @author Maheswaran Sathiamoorthy
 */
public class CodedChunk {
    //public int node;
    public int file;
    public int chunk;
    public int start;
    public int end;
    public double data;
    public int nid;

    public CodedChunk(int file1, int chunk1, int start1, int end1, double data1, int nid1) {
       // node = node1;
        file = file1;
        chunk = chunk1;
        start = start1;
        end = end1;
        data = data1;
        nid = nid1;
    }

}
