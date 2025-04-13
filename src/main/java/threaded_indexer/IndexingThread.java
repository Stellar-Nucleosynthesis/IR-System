package threaded_indexer;

import kernels.IndexerKernel;

import java.io.File;
import java.util.List;

public class IndexingThread implements Runnable {
    IndexingThread(IndexerKernel kernel, File workingDir, List<File> targetFiles) {
        this.kernel = kernel;
        this.workingDir = workingDir;
        this.targetFiles = targetFiles;
    }

    private final IndexerKernel kernel;
    private final List<File> targetFiles;
    private final File workingDir;

    @Override
    public void run() {
        try{
            for(File targetFile : targetFiles)
                kernel.analyze(targetFile);
            kernel.writeResult(workingDir);
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
