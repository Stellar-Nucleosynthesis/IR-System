package realisations.simple_index;

import postings.Posting;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static utils.encoding_utils.VariableByteEncoding.readCodedInt;
import static utils.encoding_utils.VariableByteEncoding.writeCodedInt;

public class SimplePosting implements Posting<SimplePosting> {
    public SimplePosting(int threadId, int fileId) {
        this.threadId = threadId;
        this.fileId = fileId;
    }

    private int threadId;
    private int fileId;

    @Override
    public int writePosting(DataOutputStream out) throws IOException {
        int bytesWritten = 0;
        bytesWritten += writeCodedInt(out, threadId);
        bytesWritten += writeCodedInt(out, fileId);
        return bytesWritten;
    }

    @Override
    public void readPosting(DataInputStream in) throws IOException {
        threadId = readCodedInt(in);
        fileId = readCodedInt(in);
    }

    @Override
    public void merge(SimplePosting other) {
        assert other.fileId == this.fileId && other.threadId == this.threadId;
    }

    @Override
    public void intersect(SimplePosting other) {
        assert other.fileId == this.fileId && other.threadId == this.threadId;
    }

    @Override
    public void subtract(SimplePosting other) {
        assert other.fileId == this.fileId && other.threadId == this.threadId;
    }

    @Override
    public int getFileId(){
        return fileId;
    }

    @Override
    public int getThreadId(){
        return threadId;
    }

    @Override
    public double getRating(){
        return 0;
    }

    @Override
    public int compareTo(SimplePosting other) {
        if(this.threadId != other.threadId) return Integer.compare(this.threadId, other.threadId);
        return Integer.compare(this.fileId, other.fileId);
    }

    @Override
    public boolean equals(Object o){
        if(o == null) return false;
        if(o instanceof SimplePosting){
            return this.fileId == ((SimplePosting) o).fileId && this.threadId == ((SimplePosting) o).threadId;
        }
        return false;
    }
}
