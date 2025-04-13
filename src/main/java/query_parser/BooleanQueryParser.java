package query_parser;

import retrieval_system.QueryParser;
import retrieval_system.RetrievalEngine;
import retrieval_results.RetrievalResult;
import postings.Posting;

public class BooleanQueryParser<T extends RetrievalResult<T, P>, P extends Posting<P>>
        implements QueryParser<T, P> {

    @Override
    public String[] query(RetrievalEngine<T, P> engine, String query) {
        Parser<T, P> parser = new Parser<>(engine, query);
        return engine.valueOf(parser.parse());
    }
}