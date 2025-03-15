package Realizations.Dictionaries.ThreadedDictionary.ThreadedDictionaryBuilder;

public class Tuple implements Comparable<Tuple> {
    public int termID, fileID;

    public static final int TUPLE_SIZE = Integer.BYTES * 2;

    public Tuple(int termID, int fileID) {
        this.termID = termID;
        this.fileID = fileID;
    }

    @Override
    public int compareTo(Tuple other) {
        if(other == null) return 1;
        if (this.termID != other.termID) return Integer.compare(this.termID, other.termID);
        return Integer.compare(this.fileID, other.fileID);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Tuple tuple = (Tuple) obj;
        return termID == tuple.termID && fileID == tuple.fileID;
    }

    @Override
    public int hashCode() {
        return termID ^ fileID;
    }
}