package threaded_indexer;

import kernels.IndexerKernelFactory;
import retrieval_system.Indexer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ThreadedIndexer implements Indexer {
    public ThreadedIndexer(IndexerKernelFactory factory, int threadNum) {
        this.threadNum = threadNum;
        this.factory = factory;
    }

    private final int threadNum;
    private final IndexerKernelFactory factory;

    @Override
    public void analyze(File workingDir, List<File> targetFiles) throws InterruptedException {
        Thread[] threads = new Thread[threadNum];
        List<List<File>> targets = split(targetFiles);
        for (int i = 0; i < threadNum; i++) {
            File threadDir = new File(workingDir, "thread_" + i + "_output");
            boolean res = threadDir.mkdirs();
            threads[i] = new Thread(new IndexingThread(factory.createIndexerKernel(i), threadDir, targets.get(i)));
            threads[i].start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
    }

    private List<List<File>> split(List<File> files){
        List<List<File>> res = new ArrayList<>();
        int filesPerThread = files.size() / threadNum;
        for (int i = 0; i < threadNum; i++) {
            List<File> threadTargetFiles = new ArrayList<>();
            if(i == threadNum - 1){
                threadTargetFiles = files;
            } else {
                for(int j = 0; j < filesPerThread; j++){
                    threadTargetFiles.add(files.removeFirst());
                }
            }
            res.add(threadTargetFiles);
        }
        return res;
    }

}
