package Realizations.Dictionaries.ThreadedDictionary.ThreadedDictionaryBuilder;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BufferedRecordReader {
    private final DataInputStream in;
    public String term;
    public List<Integer> posting;

    public BufferedRecordReader(File file) throws IOException {
        this.in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
        posting = new ArrayList<>();
        advance();
    }

    public boolean hasNext() {
        return term != null;
    }

    public void advance() throws IOException {
        if (in.available() < Integer.BYTES * 4) {
            term = null;
            in.close();
            return;
        }
        int stringLen = in.readInt();
        StringBuilder term = new StringBuilder();
        for (int i = 0; i < stringLen; i++) {
            term.append(in.readChar());
        }
        this.term = term.toString();
        int postingLen = in.readInt();
        posting = new ArrayList<>();
        for (int i = 0; i < postingLen; i++) {
            posting.add(in.readInt());
        }
    }

    public void close() throws IOException {
        in.close();
    }
}