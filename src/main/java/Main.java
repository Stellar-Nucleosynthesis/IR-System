import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Main {
    public static void main(String[] args) throws IOException {
        File dir = new File("src/main/java/books");
        IncidenceMatrixDictionary dict1 = new IncidenceMatrixDictionary();
        InvertedIndexDictionary dict2 = new InvertedIndexDictionary();
        for(File file : Objects.requireNonNull(dir.listFiles())){
            dict1.analyzeFile(file.getAbsolutePath());
            dict2.analyzeFile(file.getAbsolutePath());
        }
        dict1.saveAs("src/main/java/dictionaries/dictionary");
        dict2.saveAs("src/main/java/dictionaries/dictionary");

        String q1 = "flit AND after AND NOT posture";
        System.out.println(dict1.query(q1));
        System.out.println(dict2.query(q1));
    }
}
