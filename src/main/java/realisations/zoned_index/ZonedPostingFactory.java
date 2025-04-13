package realisations.zoned_index;

import postings.PostingFactory;

public class ZonedPostingFactory implements PostingFactory<ZonedPosting> {
    @Override
    public ZonedPosting create(int threadId, int fileId) {
        return new ZonedPosting(threadId, fileId);
    }
}
