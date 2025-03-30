package utils.file_parsing_utils;

import java.io.*;

public interface FileFormatParser {
    String readLine() throws IOException;

    Zone getCurrentZone();

    void close() throws IOException;
}