package QuerySystem;

import java.io.File;
import java.io.IOException;

public interface Dictionary {
    QueryResult findWord(String word);
    QueryResult findPhrase(String phrase);
    QueryResult findWordsWithin(String word1, String word2, int n);
}
