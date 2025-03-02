package QuerySystem;

import java.io.IOException;

public interface Dictionary {
    void analyze(String fileName) throws IOException;
    void saveAs(String fileName) throws IOException;
    void loadFrom(String fileName) throws IOException;

    QueryResult findWord(String word);
    QueryResult findPhrase(String phrase);
    QueryResult findWordsWithin(String word1, String word2, int n);
}
