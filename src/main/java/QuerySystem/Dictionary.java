package QuerySystem;

import java.io.File;
import java.io.IOException;

public interface Dictionary {
    void saveAs(File file) throws IOException;
    void loadFrom(File file) throws IOException;

    QueryResult findWord(String word);
    QueryResult findPhrase(String phrase);
    QueryResult findWordsWithin(String word1, String word2, int n);
}
