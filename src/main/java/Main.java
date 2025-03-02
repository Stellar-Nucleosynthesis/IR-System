import QueryParsers.PhrasalBooleanRetrQueryParser;
import QuerySystem.QuerySystem;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class Main {

    static String dictDir = "src/main/java/dictionaries";

    public static void main(String[] args) throws IOException {
        QuerySystem d1 = new QuerySystem(new CoordinateIndexDictionary(), new PhrasalBooleanRetrQueryParser());
        File bookDir = new File("src/main/java/books");
        for (File file : Objects.requireNonNull(bookDir.listFiles())) {
            d1.analyze(file.getAbsolutePath());
        }
        d1.saveAs(dictDir + "/dict1");
        System.out.println(Arrays.toString(d1.query("\"People think genius a fine thing if it enables a man\n" +
                "to write an exciting poem, or paint a picture. But in its true sense,\n" +
                "that of originality in thought and action, though no one says that it is\n" +
                "not a thing to be admired, nearly all, at heart, think that they can do\n" +
                "very well without it. Unhappily this is too natural to be wondered at.\n" +
                "Originality is the one thing which unoriginal minds cannot feel the use\n" +
                "of. They cannot see what it\"")));

        QuerySystem d2 = new QuerySystem(new BWordIndexDictionary(), new PhrasalBooleanRetrQueryParser());
        for (File file : Objects.requireNonNull(bookDir.listFiles())) {
            d2.analyze(file.getAbsolutePath());
        }
        d2.saveAs(dictDir + "/dict2");
        System.out.println(Arrays.toString(d2.query("\"People think genius a fine thing if it enables a man\n" +
                "to write an exciting poem, or paint a picture. But in its true sense,\n" +
                "that of originality in thought and action, though no one says that it is\n" +
                "not a thing to be admired, nearly all, at heart, think that they can do\n" +
                "very well without it. Unhappily this is too natural to be wondered at.\n" +
                "Originality is the one thing which unoriginal minds cannot feel the use\n" +
                "of. They cannot see what it\"")));
    }
}
