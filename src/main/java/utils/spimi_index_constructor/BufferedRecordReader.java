package utils.spimi_index_constructor;

import postings.Posting;
import postings.PostingFactory;
import postings.PostingsList;

import java.io.*;

import static utils.encoding_utils.VariableByteEncoding.readCodedInt;

public class BufferedRecordReader<T extends Posting<T>> {
    public BufferedRecordReader(File file, PostingFactory<T> factory) throws IOException {
        this.in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
        this.factory = factory;
        advance();
    }

    private final PostingFactory<T> factory;

    private final DataInputStream in;
    public String term;
    public PostingsList<T> postings = new PostingsList<>();

    public boolean hasNext() {
        return term != null;
    }

    public void advance() throws IOException {
        if (in.available() == 0) {
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
        postings = new PostingsList<>();
        postings.readPostingsList(in, factory);
    }

    public void close() throws IOException {
        in.close();
    }
}