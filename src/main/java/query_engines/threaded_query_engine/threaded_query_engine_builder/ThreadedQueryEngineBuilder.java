package query_engines.threaded_query_engine.threaded_query_engine_builder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ThreadedQueryEngineBuilder {
    public ThreadedQueryEngineBuilder(File workingDir, List<File> targetFiles, int threadNum){
        this.workingDir = workingDir;
        if(threadNum < 1) threadNum = 1;
        this.threadNum = threadNum;
        this.targetFiles = targetFiles;
    }

    private final List<File> targetFiles;
    private final File workingDir;
    private final int threadNum;

    public void startAnalysis() throws InterruptedException {
        Thread[] threads = new Thread[threadNum];

        int filesPerThread = targetFiles.size() / threadNum;
        for (int i = 0; i < threadNum; i++) {
            List<File> threadTargetFiles = new ArrayList<>();
            if(i == threadNum-1){
                threadTargetFiles = targetFiles;
            } else {
                for(int j = 0; j < filesPerThread; j++){
                    threadTargetFiles.add(targetFiles.removeFirst());
                }
            }
            threads[i] = new Thread(new IndexingThread(workingDir, threadTargetFiles, i));
            threads[i].start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
    }
}
