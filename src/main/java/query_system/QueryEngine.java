package query_system;

public interface QueryEngine<T extends QueryResult<T>> {
    T findWord(String word);
    T findPhrase(String phrase);
    T findWordsWithin(String word1, String word2, int n);
}
