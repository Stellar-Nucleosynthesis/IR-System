package threaded_retrieval_engine;

import retrieval_system.RetrievalEngine;
import retrieval_results.RetrievalResult;
import retrieval_results.RetrievalResultFactory;
import kernels.RetrievalEngineKernel;
import kernels.RetrievalEngineKernelFactory;
import postings.Posting;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadedRetrievalEngine<T extends RetrievalResult<T, P>, P extends Posting<P>> implements RetrievalEngine<T, P> {
    public ThreadedRetrievalEngine(File workingDir, RetrievalResultFactory<T, P> resFactory,
                                   RetrievalEngineKernelFactory<T, P> kernelFactory, int threadNum) throws IOException {
        this.resultFactory = resFactory;
        this.executor = Executors.newFixedThreadPool(threadNum);

        for (int i = 0; i < threadNum; i++) {
            File threadDir = new File(workingDir, "thread_" + i + "_output");
            RetrievalEngineKernel<T, P> kernel = kernelFactory.create(threadDir, i);
            T result = resFactory.create();
            RetrievalEngineThread<T, P> worker = new RetrievalEngineThread<>(result, kernel);
            kernels.add(kernel);
            workers.add(worker);
        }
    }

    private final RetrievalResultFactory<T, P> resultFactory;
    private final List<RetrievalEngineThread<T, P>> workers = new ArrayList<>();
    private final List<RetrievalEngineKernel<T, P>> kernels = new ArrayList<>();
    private final ExecutorService executor;

    @Override
    public T retrieve(String phrase) {
        List<Future<T>> futures = new ArrayList<>();
        for (RetrievalEngineThread<T, P> worker : workers) {
            futures.add(executor.submit(() -> worker.retrieve(phrase)));
        }
        T merged = resultFactory.create();
        for (Future<T> future : futures) {
            try {
                merged.merge(future.get());
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Failed to retrieve result from thread", e);
            }
        }
        return merged;
    }

    @Override
    public T retrieve(String term1, String term2, int distance) {
        throw new UnsupportedOperationException("Unsupported operation.");
    }

    @Override
    public T retrieveAll() {
        T res = resultFactory.create();
        for (RetrievalEngineKernel<T, P> kernel : kernels) {
            res.merge(kernel.retrieveAll());
        }
        return res;
    }

    @Override
    public String[] valueOf(T result) {
        List<P> postings = result.toPostingList().getPostings();
        postings.sort((p1, p2) -> -Double.compare(p1.getRating(), p2.getRating()));
        List<String> values = new ArrayList<>();
        for (P posting : postings) {
            int threadId = posting.getThreadId();
            values.add(kernels.get(threadId).getFile(posting.getFileId()));
        }
        return values.toArray(new String[0]);
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }
}