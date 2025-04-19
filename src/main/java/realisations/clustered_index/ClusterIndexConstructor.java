package realisations.clustered_index;

import postings.PostingFactory;
import postings.PostingsList;
import utils.spimi_index_constructor.SpimiIndexConstructor;
import utils.vectors.SparseVector;

import java.io.*;
import java.util.*;

import static utils.encoding_utils.VariableByteEncoding.writeCodedInt;

public class ClusterIndexConstructor extends SpimiIndexConstructor<DocumentVector> {
    public ClusterIndexConstructor(PostingFactory<DocumentVector> factory, int bufferSize) {
        super(factory, bufferSize);
    }

    private final SparseVector df = new SparseVector();
    private final SparseVector idf = new SparseVector();
    private final List<Integer> fileIds = new ArrayList<>();
    private final Map<Integer, Cluster> clusters = new HashMap<>();

    @Override
    public void addPosting(String term, DocumentVector vector) throws IOException {
        for(Integer termId : vector.getTermVector().getNonZeroEntries()){
            df.set(termId, df.get(termId) + 1);
        }
        fileIds.add(vector.getFileId());
        chooseLeader(vector);
        super.addPosting(term, vector);
    }

    @Override
    public void constructIndex(File outputDirectory) throws IOException{
        for (int index : df.getNonZeroEntries()) {
            idf.set(index, Math.log(df.get(index) / fileIds.size()));
        }
        for(Cluster cluster : clusters.values()){
            cluster.getLeader().getTermVector().multiply(idf);
        }
        super.constructIndex(outputDirectory);
        saveIdf(outputDirectory);
        saveClusters(outputDirectory);
    }

    @Override
    protected void preProcess(PostingsList<DocumentVector> postings) {
        for(DocumentVector vector : postings.getPostings()){
            vector.getTermVector().multiply(idf);
            List<Integer> leaders = new ArrayList<>(clusters.keySet());
            int minLeader = Collections.min(leaders, Comparator.comparingDouble(
                    l -> clusters.get(l).getLeader().angleTo(vector)));
            clusters.get(minLeader).addFileId(vector.getFileId());
        }
    }

    private void chooseLeader(DocumentVector vector) {
        Random random = new Random();
        int targetSize = (int) Math.floor(Math.sqrt(fileIds.size()));
        if (clusters.size() < targetSize) {
            clusters.put(vector.getFileId(), new Cluster(vector));
        } else {
            double probability = (double) targetSize / fileIds.size();
            if (random.nextDouble() < probability) {
                List<Integer> leaders = new ArrayList<>(clusters.keySet());
                int indexToReplace = leaders.get(new Random().nextInt(leaders.size()));
                clusters.put(indexToReplace, new Cluster(vector));
            }
        }
    }

    private void saveIdf(File directory) throws IOException {
        File idfFile = new File(directory, "idf.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(idfFile));
        for(Integer termId : idf.getNonZeroEntries()) {
            writer.write(termId + "\t" + idf.get(termId) + "\n");
        }
        writer.close();
    }

    private void saveClusters(File directory) throws IOException {
        File clusterFile = new File(directory, "clusters.txt");
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(clusterFile)));
        writeCodedInt(out, clusters.size());
        for(Cluster cluster : clusters.values()){
            cluster.writeCluster(out);
        }
        out.close();
    }
}
