package utils.postings;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static utils.encoding_utils.VariableByteEncoding.readCodedInt;
import static utils.encoding_utils.VariableByteEncoding.writeCodedInt;

public class LocalPosting implements Comparable<LocalPosting>{
    public LocalPosting(int fileID){
        this.fileID = fileID;
    }

    private final int fileID;

    public static int writePostingsList(DataOutputStream out, List<LocalPosting> postings) throws IOException {
        int bytesWritten = 0;
        bytesWritten += writeCodedInt(out, postings.size());
        for (LocalPosting posting : postings) {
            bytesWritten += writeCodedInt(out, posting.fileID);
        }
        return bytesWritten;
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

    public int getFileID(){
        return fileID;
    }

    @Override
    public int compareTo(LocalPosting o) {
        if(o == null) return 1;
        return Integer.compare(this.fileID, o.fileID);
    }

    @Override
    public boolean equals(Object o){
        if(o == null) return false;
        if(o instanceof LocalPosting){
            return this.fileID == ((LocalPosting) o).fileID;
        }
        return false;
    }
}
