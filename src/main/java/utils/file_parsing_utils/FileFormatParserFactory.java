package utils.file_parsing_utils;

import java.io.*;
import java.util.regex.Pattern;

public class FileFormatParserFactory {
    public static FileFormatParser getFileParser(File file) throws IOException {
        try {
            if (file.getAbsolutePath().endsWith(".txt")) {
                return new TxtParser(file);
            } else if (file.getAbsolutePath().endsWith(".fb2")) {
                return new FB2Parser(file);
            }
            throw new Exception();
        } catch (Exception e) {
            throw new IOException("Unable to create a file parser " + file.getAbsolutePath());
        }
    }

    private static class TxtParser implements FileFormatParser {
        private final BufferedReader reader;
        private Zone currentZone;

        TxtParser(File file) throws FileNotFoundException {
            this.reader = new BufferedReader(new FileReader(file));
            this.currentZone = Zone.BODY;
        }

        public String readLine() throws IOException {
            String line = reader.readLine();
            if (line == null) return null;

            if (line.startsWith("Title:")) {
                currentZone = Zone.TITLE;
            } else if (line.startsWith("Author:")) {
                currentZone = Zone.AUTHORS;
            } else {
                currentZone = Zone.BODY;
            }
            return line;
        }

        public Zone getCurrentZone() {
            return currentZone;
        }

        public void close() throws IOException {
            reader.close();
        }
    }

    private static class FB2Parser implements FileFormatParser {
        private final BufferedReader reader;
        private Zone currentZone;
        private final Pattern headerPattern = Pattern.compile("<title>");
        private final Pattern authorsPattern = Pattern.compile("<author>");
        private final Pattern bodyPattern = Pattern.compile("<body>");

        FB2Parser(File file) throws FileNotFoundException {
            this.reader = new BufferedReader(new FileReader(file));
            this.currentZone = Zone.TITLE;
        }

        public String readLine() throws IOException {
            String line = reader.readLine();
            if (line == null) return null;

            if (authorsPattern.matcher(line).find()) {
                currentZone = Zone.AUTHORS;
            } else if (bodyPattern.matcher(line).find()) {
                currentZone = Zone.BODY;
            } else if(headerPattern.matcher(line).find()) {
                currentZone = Zone.TITLE;
            }
            return line;
        }

        public Zone getCurrentZone() {
            return currentZone;
        }

        public void close() throws IOException {
            reader.close();
        }
    }

}