package kernels;

import postings.Posting;
import retrieval_results.RetrievalResult;

public interface RetrievalEngineKernel<T extends RetrievalResult<T, P>, P extends Posting<P>> {
    T retrieve(String phrase);
    T retrieve(String term1, String term2, int distance);
    T retrieveAll();
    String getFile(int fileId);
}
