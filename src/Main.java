import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Main {
    public static void main(String[] args) throws IOException {
        Dictionary dict = new Dictionary();
        File dir = new File("src/books");
        for(File file : Objects.requireNonNull(dir.listFiles())){
            dict.analyzeFile(file.getAbsolutePath());
        }

        String savedDict = "src/dictionaries/dictionary1.dict0";
        dict.saveAs(savedDict);

        Dictionary dict2 = new Dictionary(savedDict);
        System.out.println("Files: " + dict2.getFileNames());
        System.out.println("Num of words: " + dict2.getNumOfWords());
    }
}
