package utils.postings;

public class ZonedGlobalPosting implements Comparable<ZonedGlobalPosting> {
    public ZonedGlobalPosting(int threadID, ZonedLocalPosting localPosting) {
        this.threadID = threadID;
        this.localPosting = localPosting;
    }

    private final int threadID;
    private final ZonedLocalPosting localPosting;

    public int getThreadID() {
        return threadID;
    }

    public int getFileID() {
        return localPosting.getFileID();
    }

    public double getRating() { return localPosting.getRating(); }

    public void merge(ZonedGlobalPosting globalPosting) {
        localPosting.merge(globalPosting.localPosting);
    }

    public void intersect(ZonedGlobalPosting globalPosting) {
        localPosting.intersect(globalPosting.localPosting);
    }

    public void subtract() {
        localPosting.subtract();
    }

    @Override
    public int compareTo(ZonedGlobalPosting o) {
        if(o == null) return 1;
        if(this.getThreadID() != o.getThreadID()) return Integer.compare(this.getThreadID(), o.getThreadID());
        return Integer.compare(this.getFileID(), o.getFileID());
    }
}
