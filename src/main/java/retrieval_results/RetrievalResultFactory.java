package retrieval_results;


import postings.Posting;

public interface RetrievalResultFactory<T extends RetrievalResult<T, P>, P extends Posting<P>> {
    public T create();
}
