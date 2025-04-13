package postings;

public interface PostingFactory<T extends Posting<T>> {
    T create(int threadId, int fileId);
}