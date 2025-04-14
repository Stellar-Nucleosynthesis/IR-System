package realisations.clustered_index;

import kernels.RetrievalEngineKernelFactory;

import java.io.File;
import java.io.IOException;

public class ClusterRetrievalEngineKernelFactory implements RetrievalEngineKernelFactory<ClusterRetrievalResult, ClusterPosting> {
    @Override
    public ClusterRetrievalEngineKernel create(File workingDir, int threadId) throws IOException {
        return new ClusterRetrievalEngineKernel(workingDir, threadId);
    }
}
