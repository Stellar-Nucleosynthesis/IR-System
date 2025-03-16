import QuerySystem.QuerySystem;
import Realizations.Dictionaries.ThreadedDictionary.ThreadedDictionary;
import Realizations.Dictionaries.ThreadedDictionary.ThreadedDictionaryBuilder.ThreadedDictionaryBuilder;
import Realizations.Dictionaries.ThreadedDictionary.ThreadedDictionaryReader.ThreadedDictionaryReader;
import Realizations.Dictionaries.TolerantDictionaries.PermutedIndexDictionary;
import Realizations.Dictionaries.TolerantDictionaries.PrefixIndexDictionary;
import Realizations.Dictionaries.TolerantDictionaries.TrigramIndexDictionary;
import Realizations.QueryParsers.BooleanRetrQueryParser;

import java.io.*;
import java.util.*;

public class Main {

    static String dictDir = "src/main/java/dictionaries";

    public static void main(String[] args) throws IOException, InterruptedException {
        trigramJokerTest();
    }

    private static void trigramJokerTest() throws IOException {
        File books = new File("C:\\Users\\nstep\\IdeaProjects\\IR System\\src\\main\\java\\books");
        QuerySystem system = new QuerySystem(new TrigramIndexDictionary(listFilesRecursive(books)), new BooleanRetrQueryParser());
        while(true){
            System.out.println("Enter a query, enter 0 to stop");
            String query = System.console().readLine();
            if(query.equals("0")) break;

            String[] res = system.query(query);

            System.out.println(Arrays.toString(res));
            System.out.println(res.length + " results");
        }
    }

    private static void prefixJokerTest() throws IOException {
        File books = new File("C:\\Users\\nstep\\IdeaProjects\\IR System\\src\\main\\java\\books");
        QuerySystem system = new QuerySystem(new PrefixIndexDictionary(listFilesRecursive(books)), new BooleanRetrQueryParser());
        while(true){
            System.out.println("Enter a query, enter 0 to stop");
            String query = System.console().readLine();
            if(query.equals("0")) break;

            String[] res = system.query(query);

            System.out.println(Arrays.toString(res));
            System.out.println(res.length + " results");
        }
    }

    private static void permutedJokerTest() throws IOException {
        File books = new File("C:\\Users\\nstep\\IdeaProjects\\IR System\\src\\main\\java\\books");
        QuerySystem system = new QuerySystem(new PermutedIndexDictionary(listFilesRecursive(books)), new BooleanRetrQueryParser());
        while(true){
            System.out.println("Enter a query, enter 0 to stop");
            String query = System.console().readLine();
            if(query.equals("0")) break;

            String[] res = system.query(query);

            System.out.println(Arrays.toString(res));
            System.out.println(res.length + " results");
        }
    }

    private void threadedDictTest() throws InterruptedException {
        File cwd = new File("C:\\Users\\nstep\\Desktop\\Dictionary");
        long sTime = System.nanoTime();
        ThreadedDictionary dict = new ThreadedDictionary(cwd, 10);
        QuerySystem system = new QuerySystem(dict, new BooleanRetrQueryParser());
        long eTime = System.nanoTime();
        long dur = eTime - sTime;
        System.out.println("System started in " + dur/1_000_000 + "ms");

        while(true){
            System.out.println("Enter a query, enter 0 to stop");
            String query = System.console().readLine();
            if(query.equals("0")) break;

            long startTime = System.nanoTime();
            String[] res = system.query(query);
            long endTime = System.nanoTime();
            long duration = endTime - startTime;

            System.out.println(Arrays.toString(res));
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
