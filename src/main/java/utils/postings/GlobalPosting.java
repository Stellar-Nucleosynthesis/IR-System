package utils.postings;

import java.io.Serializable;

public class GlobalPosting implements Comparable<GlobalPosting> {
    public GlobalPosting(int threadID, LocalPosting localPosting) {
        this.threadID = threadID;
        this.localPosting = localPosting;
    }

    private final int threadID;
    private final LocalPosting localPosting;

    public int getThreadID() {
        return threadID;
    }

    public int getFileID() {
        return localPosting.getFileID();
    }

    public double getRating() { return localPosting.getRating(); }

    public void merge(GlobalPosting globalPosting) {
        localPosting.merge(globalPosting.localPosting);
    }

    public void intersect(GlobalPosting globalPosting) {
        localPosting.intersect(globalPosting.localPosting);
    }

    public void subtract() {
        localPosting.subtract();
    }

    @Override
    public int compareTo(GlobalPosting o) {
        if(o == null) return 1;
        if(this.getThreadID() != o.getThreadID()) return Integer.compare(this.getThreadID(), o.getThreadID());
        return Integer.compare(this.getFileID(), o.getFileID());
    }
}
