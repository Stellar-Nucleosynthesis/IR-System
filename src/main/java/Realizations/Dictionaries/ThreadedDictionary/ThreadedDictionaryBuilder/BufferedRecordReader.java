package Realizations.Dictionaries.ThreadedDictionary.ThreadedDictionaryBuilder;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static EncodingUtils.VaribleByteEncoding.readCodedInt;

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
        if (in.available() < 2) {
            term = null;
            in.close();
            return;
        }
        int stringLen = readCodedInt(in);
        StringBuilder term = new StringBuilder();
        for (int i = 0; i < stringLen; i++) {
            term.append(in.readChar());
        }
        this.term = term.toString();
        int postingLen = readCodedInt(in);
        posting = new ArrayList<>();
        for (int i = 0; i < postingLen; i++) {
            posting.add(readCodedInt(in));
        }
    }

    public void close() throws IOException {
        in.close();
    }
}