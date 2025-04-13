package realisations.zoned_index;

import kernels.IndexerKernel;
import kernels.IndexerKernelFactory;

public class ZonedIndexerKernelFactory implements IndexerKernelFactory {
    @Override
    public IndexerKernel createIndexerKernel(int threadId) {
        return new ZonedIndexerKernel(threadId);
    }
}
