import query_engines.threaded_query_engine.threaded_query_engine_reader.ThreadedQueryEngineReader.ThreadedDictQueryResult;
import query_system.QuerySystem;
import query_engines.threaded_query_engine.ThreadedQueryEngine;
import query_parsers.PhrasalBooleanRetrQueryParser;

import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        threadedDictTest();
    }

    private static void threadedDictTest() throws InterruptedException {
        File cwd = new File("C:\\Users\\nstep\\Desktop\\Dictionary");
        File bookDir = new File("C:\\Users\\nstep\\Downloads\\books");
        System.out.println(listFilesRecursive(bookDir).size());
        long sTime = System.nanoTime();
        ThreadedQueryEngine dict = new ThreadedQueryEngine(cwd, 32);
        QuerySystem<ThreadedDictQueryResult> system = new QuerySystem<>(dict, new PhrasalBooleanRetrQueryParser<>());
        long eTime = System.nanoTime();
        long dur = eTime - sTime;
        System.out.println("Index constructed in " + dur/1_000_000 + "ms");

        while(true){
            System.out.println("Enter a query, enter 0 to stop");
            String query = System.console().readLine();
            if(query.equals("0")) break;

            long startTime = System.nanoTime();
            String[] res = system.query(query);
            long endTime = System.nanoTime();
            long duration = endTime - startTime;

            System.out.println(Arrays.toString(Arrays.copyOf(res, 10)));
            System.out.println("Query completed in " + duration / 1_000_000 + "ms");
            System.out.println(res.length + " results");
        }
        dict.close();
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
