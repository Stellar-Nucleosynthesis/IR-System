package query_system;

public class QuerySystem{
    public QuerySystem(QueryEngine queryEngine, QueryParser parser) {
        this.queryEngine = queryEngine;
        this.parser = parser;
    }

    private final QueryEngine queryEngine;

    private final QueryParser parser;

    public String[] query(String query){
        QueryResult result = parser.parse(queryEngine, query);
        assert result != null;
        return result.value();
    }
}
