package kernels;

import postings.Posting;
import retrieval_results.RetrievalResult;

import java.io.File;
import java.io.IOException;

public interface RetrievalEngineKernelFactory<T extends RetrievalResult<T, P>, P extends Posting<P>> {
    RetrievalEngineKernel<T, P> create(File workingDir, int threadId) throws IOException;
}
