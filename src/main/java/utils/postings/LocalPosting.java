package utils.postings;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static utils.encoding_utils.VariableByteEncoding.readCodedInt;
import static utils.encoding_utils.VariableByteEncoding.writeCodedInt;

public record LocalPosting(int fileID) implements Comparable<LocalPosting> {
    public static int writePostingsList(DataOutputStream out, List<LocalPosting> postings) throws IOException {
        int len = 0;
        len += writeCodedInt(out, postings.size());
        for (LocalPosting posting : postings) {
            len += writeCodedInt(out, posting.fileID);
        }
        return len;
    }

    public static List<LocalPosting> readPostingsList(DataInputStream in) throws IOException {
        int len = readCodedInt(in);
        List<LocalPosting> postings = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            postings.add(new LocalPosting(readCodedInt(in)));
        }
        return postings;
    }

    public GlobalPosting toGlobalPosting(int threadID) {
        return new GlobalPosting(threadID, this);
    }

    @Override
    public int compareTo(LocalPosting o) {
        return Integer.compare(this.fileID, o.fileID);
    }
}
