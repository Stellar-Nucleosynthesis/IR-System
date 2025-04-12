package query_engines.threaded_query_engine;

import query_system.QueryEngine;
import query_engines.threaded_query_engine.threaded_query_engine_builder.ThreadedQueryEngineBuilder;
import query_engines.threaded_query_engine.threaded_query_engine_reader.ThreadedQueryEngineReader;

import java.io.*;
import java.util.List;

import static utils.file_parsing_utils.StemmingStringTokenizer.normalize;

public class ThreadedQueryEngine implements QueryEngine<ThreadedQueryEngineReader.ThreadedDictQueryResult> {
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
    public ThreadedQueryEngineReader.ThreadedDictQueryResult findWord(String word) {
        word = normalize(word);
        try{
            return reader.find(word);
        } catch(Exception e){
            return null;
        }
    }

    @Override
    public ThreadedQueryEngineReader.ThreadedDictQueryResult findPhrase(String phrase) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ThreadedQueryEngineReader.ThreadedDictQueryResult findWordsWithin(String word1, String word2, int n) {
        throw new UnsupportedOperationException();
    }
}
