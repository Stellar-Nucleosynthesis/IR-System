package FileParsingUtils;

import java.io.IOException;

public interface FileParser {
    String readLine() throws IOException;
    void close() throws IOException;
}
