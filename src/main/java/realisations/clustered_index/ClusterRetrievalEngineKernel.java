package realisations.clustered_index;

import kernels.RetrievalEngineKernel;
import postings.PostingsList;
import utils.encoding_utils.BlockedCompressedDictionary;

import java.io.*;
import java.util.*;

import static utils.encoding_utils.VariableByteEncoding.readCodedInt;
import static utils.file_parsing_utils.StemmingStringTokenizer.tokenize;

public class ClusterRetrievalEngineKernel implements RetrievalEngineKernel<ClusterRetrievalResult, ClusterPosting> {
    public ClusterRetrievalEngineKernel(File workingDir, int threadId) throws IOException {
        this.indexFile = new File(workingDir, "output.txt");
        File postingAddrFile = new File(workingDir, "postingAddr.txt");
        this.postingAddr = new BlockedCompressedDictionary(postingAddrFile);
        File termIdsFile = new File(workingDir, "termIds.txt");
        this.termIds = new BlockedCompressedDictionary(termIdsFile);
        File fileNamesFile = new File(workingDir, "fileIds.txt");
        BufferedReader reader = new BufferedReader(new FileReader(fileNamesFile));
        while (reader.ready()) {
            String[] line = reader.readLine().split("\t");
            fileNames.put(Integer.parseInt(line[1]), line[0]);
        }
        reader.close();
        File clusterFile = new File(workingDir, "clusters.txt");
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(clusterFile)));
        int clusterNum = readCodedInt(in);
        for(int i = 0; i < clusterNum; i++) {
            Cluster cluster = new Cluster();
            cluster.readCluster(in);
            clusters.add(cluster);
        }
        in.close();
        this.threadId = threadId;
    }

    private final int threadId;

    private final BlockedCompressedDictionary postingAddr;
    private final BlockedCompressedDictionary termIds;
    private final Map<Integer, String> fileNames = new HashMap<>();
    private final Set<Cluster> clusters = new HashSet<>();

    private final File indexFile;

    @Override
    public ClusterRetrievalResult retrieve(String phrase) {
        try{
            List<String> terms = tokenize(phrase);
            ClusterPosting posting = new ClusterPosting(threadId, -1);
            for (String term : terms) {
                if(termIds.containsKey(term)) {
                    posting.addTerm(termIds.get(term));
                }
            }
            posting.toUnitVector();
            Cluster nearestCluster = Collections.min(clusters, Comparator.comparingDouble(l -> l.getLeader().angleTo(posting)));
            List<ClusterPosting> postings = new ArrayList<>();
            for(int fileId : nearestCluster.getFileIds()){
                int offset = postingAddr.get(fileNames.get(fileId));
                ClusterPosting clusterPosting = getPosting(indexFile, offset);
                        new ClusterPosting(threadId, fileId);
                clusterPosting.setAngleToQuery(clusterPosting.angleTo(posting));
                postings.add(clusterPosting);
            }
            return new ClusterRetrievalResult(new PostingsList<>(postings));
        } catch(Exception e){
            return new ClusterRetrievalResult(new PostingsList<>());
        }
    }

    private ClusterPosting getPosting(File outputFile, int offset) throws IOException {
        PostingsList<ClusterPosting> postings = new PostingsList<>();
        DataInputStream in = new DataInputStream(new FileInputStream(outputFile));
        in.skipBytes(offset);
        postings.readPostingsList(in, ClusterPosting::new);
        in.close();
        return postings.getPostings().getFirst();
    }

    @Override
    public ClusterRetrievalResult retrieve(String term1, String term2, int distance) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ClusterRetrievalResult retrieveAll() {
        PostingsList<ClusterPosting> postings = new PostingsList<>();
        for(int i = 0; i < fileNames.size(); i++){
            postings.addPosting(new ClusterPosting(threadId, i));
        }
        return new ClusterRetrievalResult(postings);
    }

    @Override
    public String getFile(int fileId) {
        return fileNames.get(fileId);
    }
}
