package realizations.query_engines.threaded_query_engine.threaded_query_engine_reader;

import query_system.QueryResult;
import utils.postings.GlobalPosting;

import java.io.File;
import java.util.*;
import java.util.concurrent.Semaphore;

public class ThreadedQueryEngineReader {
    public ThreadedQueryEngineReader(File workingDir, int threadNum) {
        this.threadNum = threadNum;
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

    private final Semaphore finishWait = new Semaphore(0);

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

        public List<GlobalPosting> result;

        @Override
        public void and(QueryResult other) {
            checkArgument(other);
            List<GlobalPosting> otherList = ((ThreadedDictQueryResult) other).result;
            List<GlobalPosting> merged = new ArrayList<>();
            int i = 0, j = 0;
            while (i < result.size() && j < otherList.size()) {
                GlobalPosting a = result.get(i);
                GlobalPosting b = otherList.get(j);
                int cmp = a.compareTo(b);
                if (cmp < 0) {
                    i++;
                } else if (cmp > 0) {
                    j++;
                } else {
                    a.intersect(b);
                    merged.add(a);
                    i++;
                    j++;
                }
            }
            result = merged;
        }

        @Override
        public void or(QueryResult other) {
            checkArgument(other);
            List<GlobalPosting> otherList = ((ThreadedDictQueryResult) other).result;
            List<GlobalPosting> merged = new ArrayList<>();
            int i = 0, j = 0;
            while (i < result.size() && j < otherList.size()) {
                GlobalPosting a = result.get(i);
                GlobalPosting b = otherList.get(j);
                int cmp = a.compareTo(b);
                if (cmp < 0) {
                    merged.add(a);
                    i++;
                } else if (cmp > 0) {
                    merged.add(b);
                    j++;
                } else {
                    a.merge(b);
                    merged.add(a);
                    i++;
                    j++;
                }
            }
            while (i < result.size()) {
                merged.add(result.get(i++));
            }
            while (j < otherList.size()) {
                merged.add(otherList.get(j++));
            }
            result = merged;
        }

        @Override
        public void not() {
            Set<GlobalPosting> allFileIDs = new HashSet<>();
            for(ReadingThread thread : readingThreads){
                allFileIDs.addAll(thread.getAllFileIDs());
            }
            ArrayList<GlobalPosting> res = new ArrayList<>();
            for(GlobalPosting posting : result){
                if(!allFileIDs.contains(posting)){
                    posting.subtract();
                    res.add(posting);
                }
            }
            result = res;
        }

        @Override
        public String[] value() {
            result.sort(Comparator.comparingDouble(GlobalPosting::getRating));
            String[] res = new String[result.size()];
            for(int i = 0; i < result.size(); i++){
                GlobalPosting posting = result.get(result.size() - 1 - i);
                res[i] = readingThreads[posting.getThreadID()].getFileNameById(posting.getFileID());
            }
            return res;
        }

        public void setValue(List<GlobalPosting> value){
            result = value;
        }

        private static void checkArgument(QueryResult other) {
            if(!(other instanceof ThreadedDictQueryResult)){
                throw new IllegalArgumentException("QueryResult classes not matching");
            }
        }
    }
}
