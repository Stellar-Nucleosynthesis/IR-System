package realisations.clustered_index;

import postings.PostingsList;
import retrieval_results.RetrievalResult;

public class ClusterRetrievalResult implements RetrievalResult<ClusterRetrievalResult, ClusterPosting> {
    public ClusterRetrievalResult() {
        this.postings = new PostingsList<>();
    }

    public ClusterRetrievalResult(PostingsList<ClusterPosting> postings) {
        this.postings = postings;
    }

    private PostingsList<ClusterPosting> postings;

    @Override
    public void merge(ClusterRetrievalResult other) {
        this.postings.merge(other.postings);
    }

    @Override
    public void intersect(ClusterRetrievalResult other) {
        this.postings.intersect(other.postings);
    }

    @Override
    public void subtract(ClusterRetrievalResult other) { this.postings.subtract(other.postings); }

    @Override
    public void clear() {
        this.postings = new PostingsList<>();
    }

    @Override
    public PostingsList<ClusterPosting> toPostingsList() {
        return postings;
    }
}
