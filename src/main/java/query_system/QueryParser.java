package query_system;

public interface QueryParser {
    QueryResult parse(QueryEngine dict, String query);
}
