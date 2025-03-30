package realizations.query_engines.threaded_query_engine;

import query_system.QueryEngine;
import query_system.QueryResult;
import realizations.query_engines.threaded_query_engine.threaded_query_engine_builder.ThreadedQueryEngineBuilder;
import realizations.query_engines.threaded_query_engine.threaded_query_engine_reader.ThreadedQueryEngineReader;

import java.io.*;
import java.util.List;

import static utils.file_parsing_utils.StemmingStringTokenizer.normalize;

public class ThreadedQueryEngine implements QueryEngine {
    public ThreadedQueryEngine(File workingDir, List<File> targetFiles, int threadNum) throws InterruptedException {
        ThreadedQueryEngineBuilder builder = new ThreadedQueryEngineBuilder(workingDir, targetFiles, threadNum);
        builder.startAnalysis();
        reader = new ThreadedQueryEngineReader(workingDir, threadNum);
    }

    public ThreadedQueryEngine(File workingDir, int threadNum) throws InterruptedException {
        reader = new ThreadedQueryEngineReader(workingDir, threadNum);
    }

    private final ThreadedQueryEngineReader reader;

    public void close() throws InterruptedException {
        reader.close();
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
