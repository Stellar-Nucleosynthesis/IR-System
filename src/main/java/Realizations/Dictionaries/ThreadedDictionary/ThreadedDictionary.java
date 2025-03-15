package Realizations.Dictionaries.ThreadedDictionary;

import QuerySystem.Dictionary;
import QuerySystem.QueryResult;
import Realizations.Dictionaries.ThreadedDictionary.ThreadedDictionaryBuilder.ThreadedDictionaryBuilder;
import Realizations.Dictionaries.ThreadedDictionary.ThreadedDictionaryReader.ThreadedDictionaryReader;

import java.io.*;
import java.util.List;

import static FileParsingUtils.StemmingStringTokenizer.normalize;

public class ThreadedDictionary implements Dictionary {
    public ThreadedDictionary(File workingDir, List<File> targetFiles, int threadNum) throws InterruptedException {
        ThreadedDictionaryBuilder builder = new ThreadedDictionaryBuilder(workingDir, targetFiles, threadNum);
        builder.startAnalysis();
        reader = new ThreadedDictionaryReader(workingDir, threadNum);
    }

    public ThreadedDictionary(File workingDir, int threadNum) throws InterruptedException {
        reader = new ThreadedDictionaryReader(workingDir, threadNum);
    }

    private final ThreadedDictionaryReader reader;

    @Override
    public void saveAs(File file) throws IOException {
        throw new UnsupportedOperationException("The dictionary is already saved!");
    }

    @Override
    public void loadFrom(File file) throws IOException {
        throw new UnsupportedOperationException("Unable to load another dictionary!");
    }

    @Override
    public QueryResult findWord(String word) {
        word = normalize(word);
        try{
            return reader.find(word);
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public QueryResult findPhrase(String phrase) {
        return null;
    }

    @Override
    public QueryResult findWordsWithin(String word1, String word2, int n) {
        return null;
    }
}
