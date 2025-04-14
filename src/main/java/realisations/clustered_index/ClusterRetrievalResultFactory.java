package realisations.clustered_index;

import retrieval_results.RetrievalResultFactory;

public class ClusterRetrievalResultFactory implements RetrievalResultFactory<ClusterRetrievalResult, ClusterPosting> {
    @Override
    public ClusterRetrievalResult create() {
        return new ClusterRetrievalResult();
    }
}
