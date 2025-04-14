package retrieval_results;

import postings.Posting;
import postings.PostingsList;

public interface RetrievalResult<T extends RetrievalResult<T, P>, P extends Posting<P>> {
    void merge(T other);
    void intersect(T other);
    void subtract(T other);

    void clear();

    PostingsList<P> toPostingsList();
}
