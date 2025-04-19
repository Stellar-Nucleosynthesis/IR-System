package realisations.simple_index;

import postings.PostingsList;
import retrieval_results.RetrievalResult;

public class SimpleRetrievalResult implements RetrievalResult<SimpleRetrievalResult, SimplePosting> {
    public SimpleRetrievalResult() {
        this.postings = new PostingsList<>();
    }

    public SimpleRetrievalResult(PostingsList<SimplePosting> postings) {
        this.postings = postings;
    }

    private PostingsList<SimplePosting> postings;

    @Override
    public void merge(SimpleRetrievalResult other) {
        this.postings.merge(other.postings);
    }

    @Override
    public void intersect(SimpleRetrievalResult other) {
        this.postings.intersect(other.postings);
    }

    @Override
    public void subtract(SimpleRetrievalResult other) {
        this.postings.subtract(other.postings);
    }

    @Override
    public void clear() {
        this.postings = new PostingsList<>();
    }

    @Override
    public PostingsList<SimplePosting> toPostingsList() {
        return postings;
    }
}
