package retrieval_system;


import postings.Posting;
import retrieval_results.RetrievalResult;

public interface QueryParser<T extends RetrievalResult<T, P>, P extends Posting<P>> {
    String[] query(RetrievalEngine<T, P> engine, String query);
}
