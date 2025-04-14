package realisations.clustered_index;

import kernels.IndexerKernel;
import kernels.IndexerKernelFactory;

public class ClusterIndexerKernelFactory implements IndexerKernelFactory {
    @Override
    public IndexerKernel createIndexerKernel(int threadId) {
        return new ClusterIndexerKernel(threadId);
    }
}
