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

public class ZonedLocalPosting implements Comparable<ZonedLocalPosting> {
    public ZonedLocalPosting(int fileID){
        this.fileID = fileID;
    }

    public ZonedLocalPosting(int fileID, Zone zone){
        this.fileID = fileID;
        zones.add(zone);
    }

    private final int fileID;
    private List<Zone> zones = new ArrayList<>();

    public static int writePostingsList(DataOutputStream out, List<ZonedLocalPosting> postings) throws IOException {
        int bytesWritten = 0;
        bytesWritten += writeCodedInt(out, postings.size());
        for (ZonedLocalPosting posting : postings) {
            bytesWritten += writeCodedInt(out, posting.fileID);
            bytesWritten += writeCodedInt(out, posting.zones.size());
            for(Zone z : posting.zones){
                bytesWritten += writeCodedInt(out, z.getIndex());
            }
        }
        return bytesWritten;
    }

    public static List<ZonedLocalPosting> readPostingsList(DataInputStream in) throws IOException {
        int len = readCodedInt(in);
        List<ZonedLocalPosting> postings = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            postings.add(new ZonedLocalPosting(readCodedInt(in)));
            int numOfZones = readCodedInt(in);
            for(int j = 0; j < numOfZones; j++){
                postings.get(i).zones.add(Zone.fromIndex(readCodedInt(in)));
            }
        }
        return postings;
    }

    public ZonedGlobalPosting toGlobalPosting(int threadID) {
        return new ZonedGlobalPosting(threadID, this);
    }

    public void merge(ZonedLocalPosting posting) {
        assert posting.fileID == this.fileID;
        for(Zone z : posting.zones){
            if(!this.zones.contains(z)){
                this.zones.add(z);
            }
        }
        Collections.sort(this.zones);
    }

    public void intersect(ZonedLocalPosting posting) {
        assert posting.fileID == this.fileID;
        List<Zone> newZones = new ArrayList<>();
        for(Zone z : this.zones){
            if(posting.zones.contains(z)){
                newZones.add(z);
            }
        }
        this.zones = newZones;
        Collections.sort(zones);
    }

    public void subtract() {
        List<Zone> newZones = new ArrayList<>();
        for(Zone z : Zone.values()){
            if(!this.zones.contains(z)){
                newZones.add(z);
            }
        }
        this.zones = newZones;
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
    public int compareTo(ZonedLocalPosting o) {
        if(o == null) return 1;
        if(this.fileID != o.getFileID()) return Integer.compare(this.fileID, o.fileID);
        for(Zone z : zones){
            if(!(o.zones.contains(z))){
                return 1;
            }
        }
        for(Zone z : o.zones){
            if(!(zones.contains(z))){
                return -1;
            }
        }
        return 0;
    }

    @Override
    public boolean equals(Object o){
        if(o == null) return false;
        if(o instanceof ZonedLocalPosting){
            if(this.fileID != ((ZonedLocalPosting)o).fileID){
                return false;
            }
            for(Zone z : zones){
                if(!(((ZonedLocalPosting)o).zones.contains(z))){
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
