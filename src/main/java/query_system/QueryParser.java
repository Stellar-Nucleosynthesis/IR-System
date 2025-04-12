package query_system;

public interface QueryParser<T extends QueryResult<T>> {
    T parse(QueryEngine<T> dict, String query);
}
