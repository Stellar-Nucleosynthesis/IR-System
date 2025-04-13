package realisations.zoned_index;

import retrieval_results.RetrievalResultFactory;

public class ZonedRetrievalResultFactory implements RetrievalResultFactory<ZonedRetrievalResult, ZonedPosting> {
    @Override
    public ZonedRetrievalResult create() {
        return new ZonedRetrievalResult();
    }
}
