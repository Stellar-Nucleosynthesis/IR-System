package retrieval_system;

import postings.Posting;
import retrieval_results.RetrievalResult;

import java.util.List;

public interface RetrievalEngine<T extends RetrievalResult<T, P>, P extends Posting<P>> {
    T retrieve(String phrase);
    T retrieve(String term1, String term2, int distance);
    T retrieveAll();
    List<String> valueOf(T result);

    void close();
}
