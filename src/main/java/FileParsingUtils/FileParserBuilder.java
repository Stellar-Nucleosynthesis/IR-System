package FileParsingUtils;

import java.io.*;

public class FileParserBuilder {
    public static FileParser getFileParser(File file) throws IOException {
        try{
            if(file.getAbsolutePath().endsWith(".txt")){
                return new txtParser(file);
            }
            throw new Exception();
        } catch (Exception e){
            throw new IOException("Unable to create a file parser" + " " + file.getAbsolutePath());
        }
    }

    private static class txtParser implements FileParser {
        txtParser(File file) throws FileNotFoundException {
            reader = new BufferedReader(new FileReader(file));
        }

        BufferedReader reader;

        public String readLine() throws IOException {
            return reader.readLine();
        }

        public void close() throws IOException {
            reader.close();
        }
    }
}
