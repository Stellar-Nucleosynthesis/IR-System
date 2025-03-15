package Realizations.Dictionaries.ThreadedDictionary.ThreadedDictionaryBuilder;

import java.io.*;

public class BufferedTupleReader {
    private final DataInputStream in;
    public Tuple current;

    public BufferedTupleReader(File file) throws IOException {
        this.in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
        advance();
    }

    public boolean hasNext() {
        return current != null;
    }

    public void advance() throws IOException {
        if (in.available() < Tuple.TUPLE_SIZE) {
            current = null;
            in.close();
            return;
        }
        int termID = in.readInt();
        int fileID = in.readInt();
        current = new Tuple(termID, fileID);
    }

    public void close() throws IOException {
        in.close();
    }
}