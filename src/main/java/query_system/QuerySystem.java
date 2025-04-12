package query_system;

public class QuerySystem<T extends QueryResult<T>>{
    public QuerySystem(QueryEngine<T> queryEngine, QueryParser<T> parser) {
        this.queryEngine = queryEngine;
        this.parser = parser;
    }

    private final QueryEngine<T> queryEngine;

    private final QueryParser<T> parser;

    public String[] query(String query){
        T result = parser.parse(queryEngine, query);
        assert result != null;
        return result.value();
    }
}
