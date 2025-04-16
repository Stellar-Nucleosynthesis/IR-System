package realisations.clustered_index;

import postings.PostingFactory;

public class DocumentVectorFactory implements PostingFactory<DocumentVector> {
    @Override
    public DocumentVector create(int threadId, int fileId) {
        return new DocumentVector(threadId, fileId);
    }
}
