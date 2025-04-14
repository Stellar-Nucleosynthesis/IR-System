package realisations.clustered_index;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static utils.encoding_utils.VariableByteEncoding.readCodedInt;
import static utils.encoding_utils.VariableByteEncoding.writeCodedInt;

public class Cluster {
    Cluster(){
        this.leader = new ClusterPosting(-1, -1);
    }

    Cluster(ClusterPosting leader){
        this.leader = leader;
    }

    private final ClusterPosting leader;
    private List<Integer> fileIds = new ArrayList<>();

    public void addFileId(int fileId) {
        fileIds.add(fileId);
    }

    public ClusterPosting getLeader() {
        return this.leader;
    }

    public List<Integer> getFileIds() {
        return new ArrayList<>(fileIds);
    }

    public void writeCluster(DataOutputStream out) throws IOException {
        leader.writePosting(out);
        writeCodedInt(out, fileIds.size());
        for (Integer fileId : fileIds) {
            writeCodedInt(out, fileId);
        }
    }

    public void readCluster(DataInputStream in) throws IOException {
        leader.readPosting(in);
        fileIds = new ArrayList<>();
        int numFiles = readCodedInt(in);
        for (int i = 0; i < numFiles; i++) {
            fileIds.add(readCodedInt(in));
        }
    }
}
