import QueryParsers.PhrasalBooleanRetrQueryParser;
import QuerySystem.QuerySystem;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class Main {

    static String dictDir = "src/main/java/dictionaries";

    static File bookDir = new File("src/main/java/books");


    public static void main(String[] args) throws IOException {
        QuerySystem d1 = new QuerySystem(new CoordinateIndexDictionary(), new PhrasalBooleanRetrQueryParser());
        File bookDir = new File("src/main/java/books");
        for (File file : Objects.requireNonNull(bookDir.listFiles())) {
            d1.analyze(file.getAbsolutePath());
        }
        d1.saveAs(dictDir + "/dict1");
        System.out.println(Arrays.toString(d1.query("'georgia' OR \"In this manner a great peculiarity is given\"'")));
    }
}
