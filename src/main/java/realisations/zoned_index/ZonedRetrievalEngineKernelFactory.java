package realisations.zoned_index;

import kernels.RetrievalEngineKernelFactory;

import java.io.File;
import java.io.IOException;

public class ZonedRetrievalEngineKernelFactory implements RetrievalEngineKernelFactory<ZonedRetrievalResult, ZonedPosting> {
    @Override
    public ZonedRetrievalEngineKernel create(File workingDir, int threadId) throws IOException {
        return new ZonedRetrievalEngineKernel(workingDir, threadId);
    }
}
