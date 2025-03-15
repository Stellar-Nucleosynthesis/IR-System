package Realizations.Dictionaries.ThreadedDictionary.ThreadedDictionaryReader;

import QuerySystem.QueryResult;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

public class ThreadedDictionaryReader {
    public ThreadedDictionaryReader(File workingDir, int threadNum) {
        this.threadNum = threadNum;
        this.finishWait = new Semaphore(0);
        results = new ThreadedDictQueryResult[threadNum];
        readingThreads = new ReadingThread[threadNum];
        for(int i = 0; i < threadNum; i++){
            results[i] = new ThreadedDictQueryResult();
            readingThreads[i] = new ReadingThread(i, workingDir, results[i], finishWait);
            Thread readingThread = new Thread(readingThreads[i]);
            readingThread.start();
        }
    }

    private final ReadingThread[] readingThreads;

    private final Semaphore finishWait;

    private final int threadNum;
    private final ThreadedDictQueryResult[] results;

    public QueryResult find(String word) throws InterruptedException {
        for(ThreadedDictQueryResult result : results){
            result.result.clear();
        }
        for(ReadingThread readingThread : readingThreads){
            readingThread.setCurrentWord(word);
            readingThread.unlockThread();
        }
        for(int i = 0; i < threadNum; i++){
            finishWait.acquire();
        }
        ThreadedDictQueryResult result = new ThreadedDictQueryResult();
        for(ThreadedDictQueryResult res : results){
            result.or(res);
        }
        return result;
    }

    public void close() throws InterruptedException {
        for(ReadingThread readingThread : readingThreads){
            readingThread.signalEnd();
            readingThread.unlockThread();
        }
    }

    class ThreadedDictQueryResult implements QueryResult {
        ThreadedDictQueryResult(){
            result = new ArrayList<>();
        }

        public List<Long> result;

        @Override
        public void and(QueryResult other) {
            checkArgument(other);
            Set<Long> thatResult = new HashSet<>(((ThreadedDictQueryResult)other).result);
            ArrayList<Long> res = new ArrayList<>();
            for(Long l : result){
                if(thatResult.contains(l)){
                    res.add(l);
                }
            }
            result = res;
        }

        @Override
        public void or(QueryResult other) {
            checkArgument(other);
            Set<Long> thatResult = new HashSet<>(((ThreadedDictQueryResult)other).result);
            thatResult.addAll(result);
            result = new ArrayList<>(thatResult);
        }

        @Override
        public void not() {
            Set<Long> allFileIDs = new HashSet<>();
            for(ReadingThread thread : readingThreads){
                allFileIDs.addAll(thread.getAllFileIDs());
            }
            ArrayList<Long> res = new ArrayList<>();
            for(Long l : result){
                if(!allFileIDs.contains(l)){
                    res.add(l);
                }
            }
            result = res;
        }

        @Override
        public String[] value() {
            String[] res = new String[result.size()];
            for(int i = 0; i < result.size(); i++){
                long ID = result.get(i);
                int threadID = (int) (ID >> 32);
                int fileID = (int) ID;
                res[i] = readingThreads[threadID].getFileNameById(fileID);
            }
            return res;
        }

        public void setValue(List<Long> value){
            result = value;
        }

        private static void checkArgument(QueryResult other) {
            if(!(other instanceof ThreadedDictQueryResult)){
                throw new IllegalArgumentException("QueryResult classes not matching");
            }
        }
    }
}
