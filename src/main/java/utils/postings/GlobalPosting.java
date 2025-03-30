package utils.postings;

public class GlobalPosting {
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
}
