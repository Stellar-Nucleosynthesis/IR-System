package kernels;

public interface IndexerKernelFactory {
    IndexerKernel createIndexerKernel(int threadId);
}
