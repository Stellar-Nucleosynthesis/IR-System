import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Main {
    public static void main(String[] args) throws IOException {
        //File dir = new File("src/books");
        //for(File file : Objects.requireNonNull(dir.listFiles())){
        //    dict.analyzeFile(file.getAbsolutePath());
        //}
        IncidenceMatrixDictionary dict1 = new IncidenceMatrixDictionary("src/dictionaries/dictionary1.dict1");
        InvertedIndexDictionary dict2 = new InvertedIndexDictionary("src/dictionaries/dictionary1.dict0");

        String q1 = "flit AND after AND NOT posture";
        System.out.println(dict1.query(q1).equals(dict2.query(q1)));
    }
}
