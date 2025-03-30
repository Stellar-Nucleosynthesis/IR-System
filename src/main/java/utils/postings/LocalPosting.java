package utils.postings;

import utils.file_parsing_utils.Zone;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static utils.encoding_utils.VariableByteEncoding.readCodedInt;
import static utils.encoding_utils.VariableByteEncoding.writeCodedInt;

public class LocalPosting implements Comparable<LocalPosting> {
    public LocalPosting(int fileID){
        this.fileID = fileID;
    }

    public LocalPosting(int fileID, Zone zone){
        this.fileID = fileID;
        zones.add(zone);
    }

    private final int fileID;
    private final List<Zone> zones = new ArrayList<>();

    public static int writePostingsList(DataOutputStream out, List<LocalPosting> postings) throws IOException {
        int bytesWritten = 0;
        bytesWritten += writeCodedInt(out, postings.size());
        for (LocalPosting posting : postings) {
            bytesWritten += writeCodedInt(out, posting.fileID);
            bytesWritten += writeCodedInt(out, posting.zones.size());
            for(Zone z : posting.zones){
                bytesWritten += writeCodedInt(out, z.getIndex());
            }
        }
        return bytesWritten;
    }

    public static List<LocalPosting> readPostingsList(DataInputStream in) throws IOException {
        int len = readCodedInt(in);
        List<LocalPosting> postings = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            postings.add(new LocalPosting(readCodedInt(in)));
            int numOfZones = readCodedInt(in);
            for(int j = 0; j < numOfZones; j++){
                postings.get(i).zones.add(Zone.fromIndex(readCodedInt(in)));
            }
        }
        return postings;
    }

    public GlobalPosting toGlobalPosting(int threadID) {
        return new GlobalPosting(threadID, this);
    }

    public void merge(LocalPosting posting) {
        assert posting.fileID == this.fileID;
        for(Zone z : posting.zones){
            if(!this.zones.contains(z)){
                this.zones.add(z);
            }
        }
        Collections.sort(zones);
    }

    public int getFileID(){
        return fileID;
    }

    public double getRating(){
        double rating = 0.0;
        for(Zone z : zones){
            rating += z.getValue();
        }
        return rating;
    }

    @Override
    public int compareTo(LocalPosting o) {
        if(o == null) return 1;
        return Integer.compare(this.fileID, o.fileID);
    }

    @Override
    public boolean equals(Object o){
        if(o == null) return false;
        if(o instanceof LocalPosting){
            if(this.fileID != ((LocalPosting)o).fileID){
                return false;
            }
            for(Zone z : zones){
                if(!(((LocalPosting)o).zones.contains(z))){
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
