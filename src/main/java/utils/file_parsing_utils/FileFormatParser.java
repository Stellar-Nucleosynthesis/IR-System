package utils.file_parsing_utils;

import java.io.IOException;

public interface FileFormatParser {
    String readLine() throws IOException;
    void close() throws IOException;
}
