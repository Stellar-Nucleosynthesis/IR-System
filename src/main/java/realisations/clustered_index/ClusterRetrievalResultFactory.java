package realisations.clustered_index;

import retrieval_results.RetrievalResultFactory;

public class ClusterRetrievalResultFactory implements RetrievalResultFactory<ClusterRetrievalResult, DocumentVector> {
    @Override
    public ClusterRetrievalResult create() {
        return new ClusterRetrievalResult();
    }
}
