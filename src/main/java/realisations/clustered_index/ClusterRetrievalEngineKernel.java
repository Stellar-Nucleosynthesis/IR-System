package realisations.clustered_index;

import kernels.RetrievalEngineKernel;
import postings.PostingsList;
import utils.encoding_utils.BlockedCompressedDictionary;

import java.io.*;
import java.util.*;

import static java.lang.Integer.parseInt;
import static utils.encoding_utils.VariableByteEncoding.readCodedInt;
import static utils.file_parsing_utils.StemmingStringTokenizer.tokenize;

public class ClusterRetrievalEngineKernel implements RetrievalEngineKernel<ClusterRetrievalResult, DocumentVector> {
    public ClusterRetrievalEngineKernel(File workingDir, int threadId) throws IOException {
        this.indexFile = new File(workingDir, "index.txt");

        File postingAddrFile = new File(workingDir, "postingAddr.txt");
        this.postingAddr = new BlockedCompressedDictionary(postingAddrFile);

        File termIdsFile = new File(workingDir, "termIds.txt");
        this.termIds = new BlockedCompressedDictionary(termIdsFile);

        File fileNamesFile = new File(workingDir, "fileIds.txt");
        BufferedReader reader = new BufferedReader(new FileReader(fileNamesFile));
        while (reader.ready()) {
            String[] line = reader.readLine().split("\t");
            fileNames.put(parseInt(line[1]), line[0]);
        }
        reader.close();

        File idfFile = new File(workingDir, "idf.txt");
        BufferedReader idfReader = new BufferedReader(new FileReader(idfFile));
        while (idfReader.ready()) {
            String[] line = idfReader.readLine().split("\t");
            idfs.put(Integer.valueOf(line[0]), Double.parseDouble(line[1]));
        }
        idfReader.close();

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
    private final Map<Integer, Double> idfs = new HashMap<>();
    private final Map<Integer, String> fileNames = new HashMap<>();
    private final Set<Cluster> clusters = new HashSet<>();

    private final File indexFile;

    @Override
    public ClusterRetrievalResult retrieve(String phrase) {
        try{
            List<String> terms = tokenize(phrase);
            DocumentVector queryVector = new DocumentVector(threadId, -1);
            for (String term : terms) {
                if(termIds.containsKey(term)) {
                    int termId = termIds.get(term);
                    queryVector.addToIndex(termId, idfs.get(termId));
                }
            }
            Cluster nearestCluster = Collections.min(clusters,
                    Comparator.comparingDouble(l -> l.getLeader().angleTo(queryVector)));
            List<DocumentVector> postings = new ArrayList<>();
            for(int fileId : nearestCluster.getFileIds()){
                int offset = postingAddr.get(fileNames.get(fileId));
                DocumentVector documentVector = getPosting(indexFile, offset);
                        new DocumentVector(threadId, fileId);
                documentVector.setAngleToQuery(documentVector.angleTo(queryVector));
                postings.add(documentVector);
            }
            return new ClusterRetrievalResult(new PostingsList<>(postings));
        } catch(Exception e){
            return new ClusterRetrievalResult(new PostingsList<>());
        }
    }

    private DocumentVector getPosting(File outputFile, int offset) throws IOException {
        PostingsList<DocumentVector> postings = new PostingsList<>();
        DataInputStream in = new DataInputStream(new FileInputStream(outputFile));
        in.skipBytes(offset);
        postings.readPostingsList(in, DocumentVector::new);
        in.close();
        return postings.getPostings().getFirst();
    }

    @Override
    public ClusterRetrievalResult retrieve(String term1, String term2, int distance) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ClusterRetrievalResult retrieveAll() {
        PostingsList<DocumentVector> postings = new PostingsList<>();
        for(int i = 0; i < fileNames.size(); i++){
            postings.addPosting(new DocumentVector(threadId, i));
        }
        return new ClusterRetrievalResult(postings);
    }

    @Override
    public String getFile(int fileId) {
        return fileNames.get(fileId);
    }
}
