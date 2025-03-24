package query_system;

public interface QueryEngine {
    QueryResult findWord(String word);
    QueryResult findPhrase(String phrase);
    QueryResult findWordsWithin(String word1, String word2, int n);
}
