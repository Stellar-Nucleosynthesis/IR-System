package threaded_retrieval_engine;

import retrieval_results.RetrievalResult;
import kernels.RetrievalEngineKernel;
import postings.Posting;

public class RetrievalEngineThread<T extends RetrievalResult<T, P>, P extends Posting<P>> {
    public RetrievalEngineThread(T result, RetrievalEngineKernel<T, P> kernel) {
        this.result = result;
        this.kernel = kernel;
    }

    private final T result;
    private final RetrievalEngineKernel<T, P> kernel;

    public T retrieve(String phrase) {
        result.clear();
        result.merge(kernel.retrieve(phrase));
        return result;
    }
}