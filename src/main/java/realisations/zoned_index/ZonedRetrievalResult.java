package realisations.zoned_index;

import postings.PostingsList;
import retrieval_results.RetrievalResult;

public class ZonedRetrievalResult implements RetrievalResult<ZonedRetrievalResult, ZonedPosting> {
    public ZonedRetrievalResult() {
        this.postings = new PostingsList<>();
    }

    public ZonedRetrievalResult(PostingsList<ZonedPosting> postings) {
        this.postings = postings;
    }

    private PostingsList<ZonedPosting> postings;

    @Override
    public void merge(ZonedRetrievalResult other) {
        this.postings.merge(other.postings);
    }

    @Override
    public void intersect(ZonedRetrievalResult other) {
        this.postings.intersect(other.postings);
    }

    @Override
    public void subtract(ZonedRetrievalResult other) { this.postings.subtract(other.postings); }

    @Override
    public void clear() {
        this.postings = new PostingsList<>();
    }

    @Override
    public PostingsList<ZonedPosting> toPostingsList() {
        return postings;
    }
}
