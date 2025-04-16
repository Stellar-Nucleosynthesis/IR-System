package realisations.clustered_index;

import postings.Posting;
import utils.vectors.SparseVector;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;

import static utils.encoding_utils.VariableByteEncoding.readCodedInt;
import static utils.encoding_utils.VariableByteEncoding.writeCodedInt;

public class DocumentVector implements Posting<DocumentVector>{
    public DocumentVector(int threadId, int fileId) {
        this.threadId = threadId;
        this.fileId = fileId;
    }

    private int threadId;
    private int fileId;
    private SparseVector termVector = new SparseVector();

    private double angleToQuery = Double.MAX_VALUE;

    @Override
    public int writePosting(DataOutputStream out) throws IOException {
        int bytesWritten = 0;
        bytesWritten += writeCodedInt(out, threadId);
        bytesWritten += writeCodedInt(out, fileId);
        Set<Integer> entries = termVector.getNonZeroEntries();
        bytesWritten += writeCodedInt(out, entries.size());
        for(int index : entries) {
            bytesWritten += writeCodedInt(out, index);
            out.writeDouble(termVector.get(index));
            bytesWritten += Double.BYTES;
        }
        return bytesWritten;
    }

    @Override
    public void readPosting(DataInputStream in) throws IOException {
        threadId = readCodedInt(in);
        fileId = readCodedInt(in);
        termVector = new SparseVector();
        int size = readCodedInt(in);
        for(int i = 0; i < size; i++){
            int index = readCodedInt(in);
            termVector.set(index, in.readDouble());
        }
    }

    @Override
    public int getFileId() {
        return fileId;
    }

    @Override
    public int getThreadId() {
        return threadId;
    }

    public void setAngleToQuery(double angleToQuery) {
        this.angleToQuery = angleToQuery;
    }

    @Override
    public double getRating(){
        return 1 / angleToQuery;
    }

    public SparseVector getTermVector() {
        return termVector;
    }

    public void addToIndex(int index, double value) {
        termVector.set(index, termVector.get(index) + value);
    }

    public double angleTo(DocumentVector other){
        return this.termVector.angleTo(other.termVector);
    }

    public void toUnitVector(){
        this.termVector.toUnitVector();
    }

    @Override
    public void merge(DocumentVector other) {
        assert other.fileId == this.fileId && other.threadId == this.threadId;
        termVector.add(other.termVector);
    }

    @Override
    public void intersect(DocumentVector other) {
        assert other.fileId == this.fileId && other.threadId == this.threadId;
        termVector.multiply(other.termVector);
    }

    @Override
    public void subtract(DocumentVector other) {
        assert other.fileId == this.fileId && other.threadId == this.threadId;
        other.termVector.multiply(-1);
        termVector.add(other.termVector);
        other.termVector.multiply(-1);
    }

    @Override
    public int compareTo(DocumentVector other) {
        if(this.threadId != other.threadId) return Integer.compare(this.threadId, other.threadId);
        return Integer.compare(this.fileId, other.fileId);
    }

    @Override
    public boolean equals(Object o){
        if(o == null) return false;
        if(o instanceof DocumentVector){
            return this.fileId == ((DocumentVector) o).fileId && this.threadId == ((DocumentVector) o).threadId;
        }
        return false;
    }
}
