package realisations.clustered_index;

import postings.PostingFactory;

public class ClusterPostingFactory implements PostingFactory<ClusterPosting> {
    @Override
    public ClusterPosting create(int threadId, int fileId) {
        return new ClusterPosting(threadId, fileId);
    }
}
