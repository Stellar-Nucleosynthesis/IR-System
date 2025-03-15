import Realizations.Dictionaries.ThreadedDictionary.ThreadedDictionary;
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
        System.out.println("DURATION: " + duration / 1_000_000_000 + " s");
    }

    private static void test3() throws IOException {
        HashSet<String> terms = new HashSet<>();
        for(int i = 0; i < 10; i++){
            File cwd = new File("C:\\Users\\nstep\\Desktop\\Dictionary");
            File termIDsFile = new File(cwd, "group" + i + "termIDs.txt");
            BufferedReader termIDbr = new BufferedReader(new FileReader(termIDsFile));
            String line = termIDbr.readLine();
            while (line != null) {
                String[] tokens = line.split("\t");
                terms.add(tokens[0]);
                line = termIDbr.readLine();
            }
            termIDbr.close();
        }
        System.out.println(terms.size());
    }

    private static void queryTest2() throws InterruptedException, IOException {
        File bookDir = new File("C:\\Users\\nstep\\Downloads\\books\\books");
        File cwd = new File("C:\\Users\\nstep\\Desktop\\Dictionary");
//        ThreadedDictionaryBuilder dict = new ThreadedDictionaryBuilder(cwd, listFilesRecursive(bookDir), 10);
//        dict.startAnalysis();
        ThreadedDictionary dict = new ThreadedDictionary(cwd, 10);
        String[] words = {"georgia", "a", "the", "of", "on"};
        for(int i = 0; i < 5; i++) {
            long startTime = System.nanoTime();
            dict.findWord(words[i]).value();
            //System.out.println(Arrays.toString(res));

            long endTime = System.nanoTime();
            long duration = endTime - startTime;
            System.out.println("DURATION: " + duration / 1000000 + " ms");
        }
    }

    private static void queryTest1() throws IOException {
        //QuerySystem d1 = new QuerySystem(new CoordinateIndexDictionary(), new PhrasalBooleanRetrQueryParser());
        //File bookDir = new File("src/main/java/books");
        //for (File file : Objects.requireNonNull(bookDir.listFiles())) {
        //    d1.analyze(file.getAbsolutePath());
        //}
        //d1.saveAs(dictDir + "/dict1");
        //System.out.println(Arrays.toString(d1.query("'georgian'")));
    }

    private static List<File> listFilesRecursive(File directory) {
        List<File> fileList = new ArrayList<>();
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        fileList.addAll(listFilesRecursive(file)); // Рекурсивний виклик для піддиректорії
                    } else {
                        fileList.add(file); // Додаємо файл до списку
                    }
                }
            }
        }
        return fileList;
    }
}
