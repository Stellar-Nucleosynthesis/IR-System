package retrieval_system;

import postings.Posting;
import retrieval_results.RetrievalResult;

import java.util.Arrays;

public class QuerySystem<T extends RetrievalResult<T, P>, P extends Posting<P>> {
    public QuerySystem(RetrievalEngine<T, P> retrievalEngine, QueryParser<T, P> parser) {
        this.retrievalEngine = retrievalEngine;
        this.parser = parser;
    }

    private final RetrievalEngine<T, P> retrievalEngine;
    private final QueryParser<T, P> parser;

    public String[] query(String query, int limit){
        String[] res = parser.query(retrievalEngine, query);
        return Arrays.copyOf(res, limit);
    }
}
