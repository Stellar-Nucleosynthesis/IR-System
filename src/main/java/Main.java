import realisations.clustered_index.*;
import threaded_indexer.ThreadedIndexer;
import retrieval_system.Indexer;
import retrieval_system.QuerySystem;
import realisations.zoned_index.ZonedIndexerKernelFactory;
import realisations.zoned_index.ZonedRetrievalEngineKernelFactory;
import query_parser.BooleanQueryParser;
import threaded_retrieval_engine.ThreadedRetrievalEngine;
import realisations.zoned_index.ZonedRetrievalResult;
import realisations.zoned_index.ZonedRetrievalResultFactory;
import realisations.zoned_index.ZonedPosting;

import java.io.*;
import java.util.*;

public class Main {
    static File cwd = new File("C:\\Users\\nstep\\Desktop\\Dictionary");
    static File bookDir = new File("C:\\Users\\nstep\\Downloads\\books");

    public static void main(String[] args) throws InterruptedException, IOException {
        threadedDictTest();
    }

    private static void threadedDictTest() throws InterruptedException, IOException {
        System.out.println(listFilesRecursive(bookDir).size() + " files total");

        long sTime = System.nanoTime();

        Indexer indexer = new ThreadedIndexer(ClusterIndexerKernel::new, 32);
        //indexer.analyze(cwd, listFilesRecursive(bookDir));

        long eTime = System.nanoTime();
        long dur = eTime - sTime;
        System.out.println("Index constructed in " + dur/1_000_000 + " ms");

        ThreadedRetrievalEngine<ClusterRetrievalResult, ClusterPosting> engine = new ThreadedRetrievalEngine<>(
                cwd, ClusterRetrievalResult::new, ClusterRetrievalEngineKernel::new, 32);
        QuerySystem<ClusterRetrievalResult, ClusterPosting> system = new QuerySystem<>(engine, new BooleanQueryParser<>());

        while(true){
            System.out.println("Enter a query, enter 0 to stop");
            String query = System.console().readLine();
            if(query.equals("0")) break;

            long startTime = System.nanoTime();
            List<String> res = system.query(query, 10);
            long endTime = System.nanoTime();
            long duration = endTime - startTime;

            System.out.println(res);
            System.out.println("Query completed in " + duration / 1_000_000 + "ms");
            System.out.println(res.size() + " results");
        }

        engine.close();
    }

    private static List<File> listFilesRecursive(File directory) {
        List<File> fileList = new ArrayList<>();
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        fileList.addAll(listFilesRecursive(file));
                    } else {
                        fileList.add(file);
                    }
                }
            }
        }
        return fileList;
    }
}
