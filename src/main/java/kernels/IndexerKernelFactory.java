package kernels;

public interface IndexerKernelFactory {
    IndexerKernel createIndexerKernel(int threadId, int bufferSize);
}
