package retrieval_system;


import postings.Posting;
import retrieval_results.RetrievalResult;

import java.util.List;

public interface QueryParser<T extends RetrievalResult<T, P>, P extends Posting<P>> {
    List<String> query(RetrievalEngine<T, P> engine, String query);
}
