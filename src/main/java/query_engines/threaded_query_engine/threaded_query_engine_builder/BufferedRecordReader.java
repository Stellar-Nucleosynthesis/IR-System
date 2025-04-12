package query_engines.threaded_query_engine.threaded_query_engine_builder;

import utils.postings.ZonedLocalPosting;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static utils.encoding_utils.VariableByteEncoding.readCodedInt;

public class BufferedRecordReader {
    private final DataInputStream in;
    public String term;
    public List<ZonedLocalPosting> postings;

    public BufferedRecordReader(File file) throws IOException {
        this.in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
        postings = new ArrayList<>();
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
        postings = ZonedLocalPosting.readPostingsList(in);
    }

    public void close() throws IOException {
        in.close();
    }
}