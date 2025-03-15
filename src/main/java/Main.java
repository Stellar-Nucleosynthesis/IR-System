import Realizations.Dictionaries.ThreadedDictionary.ThreadedDictionaryBuilder.ThreadedDictionaryBuilder;

import java.io.*;
import java.util.*;

public class Main {

    static String dictDir = "src/main/java/dictionaries";

    public static void main(String[] args) throws IOException, InterruptedException {
        long startTime = System.nanoTime();

        queryTest2();

        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        System.out.println("INDEX CONSTRUCTED");
        System.out.println("DURATION: " + duration / 1_000_000_000 + " s");
    }

    private static void queryTest2() throws InterruptedException, IOException {
        File bookDir = new File("C:\\Users\\nstep\\Downloads\\books\\books");
        File cwd = new File("C:\\Users\\nstep\\Desktop\\Dictionary");
        ThreadedDictionaryBuilder dict = new ThreadedDictionaryBuilder(cwd, listFilesRecursive(bookDir), 10);
        dict.startAnalysis();
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
