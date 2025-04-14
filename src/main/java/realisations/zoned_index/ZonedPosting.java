package realisations.zoned_index;

import postings.Posting;
import utils.file_parsing_utils.Zone;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static utils.encoding_utils.VariableByteEncoding.readCodedInt;
import static utils.encoding_utils.VariableByteEncoding.writeCodedInt;

public class ZonedPosting implements Posting<ZonedPosting> {
    public ZonedPosting(int threadId, int fileId) {
        this.threadId = threadId;
        this.fileId = fileId;
    }

    private int threadId;
    private int fileId;
    private List<Zone> zones = new ArrayList<>();

    @Override
    public int writePosting(DataOutputStream out) throws IOException {
        int bytesWritten = 0;
        bytesWritten += writeCodedInt(out, threadId);
        bytesWritten += writeCodedInt(out, fileId);
        bytesWritten += writeCodedInt(out, zones.size());
        for(Zone z : zones){
            bytesWritten += writeCodedInt(out, z.getIndex());
        }
        return bytesWritten;
    }

    @Override
    public void readPosting(DataInputStream in) throws IOException {
        threadId = readCodedInt(in);
        fileId = readCodedInt(in);
        zones.clear();
        int numOfZones = readCodedInt(in);
        for(int j = 0; j < numOfZones; j++){
            zones.add(Zone.fromIndex(readCodedInt(in)));
        }
    }

    @Override
    public void merge(ZonedPosting other) {
        assert other.fileId == this.fileId && other.threadId == this.threadId;
        for(Zone z : other.zones){
            if(!this.zones.contains(z)){
                this.zones.add(z);
            }
        }
        Collections.sort(this.zones);
    }

    @Override
    public void intersect(ZonedPosting other) {
        assert other.fileId == this.fileId && other.threadId == this.threadId;
        List<Zone> newZones = new ArrayList<>();
        for(Zone z : this.zones){
            if(other.zones.contains(z)){
                newZones.add(z);
            }
        }
        this.zones = newZones;
        Collections.sort(zones);
    }

    @Override
    public void subtract(ZonedPosting other) {
        assert other.fileId == this.fileId && other.threadId == this.threadId;
        List<Zone> newZones = new ArrayList<>();
        for(Zone z : other.zones){
            if(!this.zones.contains(z)){
                newZones.add(z);
            }
        }
        this.zones = newZones;
        Collections.sort(zones);
    }

    @Override
    public int getFileId(){
        return fileId;
    }

    @Override
    public int getThreadId(){
        return threadId;
    }

    public void addZone(Zone z){
        if(!this.zones.contains(z)){
            this.zones.add(z);
        }
        Collections.sort(this.zones);
    }

    @Override
    public double getRating(){
        double rating = 0.0;
        for(Zone z : zones){
            rating += z.getValue();
        }
        return rating;
    }

    @Override
    public int compareTo(ZonedPosting other) {
        if(this.threadId != other.threadId) return Integer.compare(this.threadId, other.threadId);
        return Integer.compare(this.fileId, other.fileId);
    }

    @Override
    public boolean equals(Object o){
        if(o == null) return false;
        if(o instanceof ZonedPosting){
            return this.fileId == ((ZonedPosting) o).fileId && this.threadId == ((ZonedPosting) o).threadId;
        }
        return false;
    }
}
