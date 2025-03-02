package QuerySystem;

public interface QueryResult {
    void and(QueryResult other);
    void or(QueryResult other);
    void not();
    String[] value();
}
