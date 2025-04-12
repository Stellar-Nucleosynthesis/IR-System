package query_system;

public interface QueryResult<T extends QueryResult<T>> {
    void and(T other);
    void or(T other);
    void not();
    String[] value();
}
