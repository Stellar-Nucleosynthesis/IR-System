package realisations.clustered_index;

import kernels.IndexerKernel;
import postings.PostingsList;
import utils.encoding_utils.BlockedCompressedDictionary;
import utils.encoding_utils.BlockedDictionaryCompressor;
import utils.file_parsing_utils.FileFormatParser;
import utils.file_parsing_utils.FileFormatParserFactory;
import utils.file_parsing_utils.StemmingStringTokenizer;
import utils.spimi_index_constructor.SpimiIndexConstructor;
import utils.vectors.SparseVector;

import java.io.*;
import java.security.InvalidKeyException;
import java.util.*;

import static utils.encoding_utils.VariableByteEncoding.writeCodedInt;

public class ClusterIndexerKernel implements IndexerKernel {
    public ClusterIndexerKernel(int threadId){
        this.threadId = threadId;
        indexConstructor = new SpimiIndexConstructor<>(new ClusterPostingFactory());
    }

    private final int threadId;
    private final Map<String, Integer> fileIds = new HashMap<>();
    private final Map<String, Integer> termIds = new HashMap<>();
    private final SpimiIndexConstructor<ClusterPosting> indexConstructor;

    @Override
    public void analyze(File file) throws IOException {
        FileFormatParser reader = FileFormatParserFactory.getFileParser(file);
        int fileId = fileIds.size();
        fileIds.put(file.getAbsolutePath(), fileId);
        ClusterPosting vector = new ClusterPosting(threadId, fileId);
        String line = reader.readLine();
        while (line != null) {
            for(String term : StemmingStringTokenizer.tokenize(line)) {
                termIds.putIfAbsent(term, termIds.size());
                int termId = termIds.get(term);
                vector.addToIndex(termId, 1);
            }
            line = reader.readLine();
        }
        indexConstructor.addPosting(file.getAbsolutePath(), vector);
        reader.close();
    }

    @Override
    public void writeResult(File directory) throws IOException {
        indexConstructor.constructIndex(directory);
        File termIdsFile = new File(directory, "termIds.txt");
        BlockedDictionaryCompressor.writeCompressedMapping(termIdsFile, this.termIds);
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(directory, "fileIds.txt")));
        for(String fileName : fileIds.keySet()) {
            writer.write(fileName + "\t" + fileIds.get(fileName) + "\n");
        }
        try{
            calculateIdf(directory);
            constructClusters(directory);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        writer.close();
    }

    private void calculateIdf(File directory) throws IOException {
        SparseVector idf = new SparseVector();
        File outputFile = new File(directory, "output.txt");
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(outputFile)));
        for(int i = 0; i < fileIds.size(); i++) {
            ClusterPosting posting = getNextPosting(in);
            for(Integer termId : posting.getTermVector().getNonZeroEntries()){
                idf.set(termId, idf.get(termId) + 1);
            }
        }

        in.close();
        List<Integer> indices = new ArrayList<>(idf.getNonZeroEntries());
        for (int index : indices) {
            idf.set(index, Math.log(idf.get(index) / fileIds.size()));
        }

        File indexFile = new File(directory, "index.txt");
        in = new DataInputStream(new BufferedInputStream(new FileInputStream(outputFile)));
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(indexFile)));
        for(int i = 0; i < fileIds.size(); i++) {
            ClusterPosting posting = getNextPosting(in);
            List<Integer> termIds = new ArrayList<>(posting.getTermVector().getNonZeroEntries());
            SparseVector termVector = posting.getTermVector();
            for(Integer termId : termIds){
                double tf = termVector.get(termId);
                posting.getTermVector().directSet(termId, tf * idf.get(termId));
            }
            new PostingsList<>(List.of(posting)).writePostingsList(out);
        }
        in.close();
        out.close();
        boolean removed = outputFile.delete();
        if(!removed) throw new RuntimeException("Failed to delete output file");

        File idfFile = new File(directory, "idf.txt");
        Map<String, Double> idfMapping = new HashMap<>();
        for(String term : termIds.keySet()) {
            idfMapping.put(term, idf.get(termIds.get(term)));
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(idfFile));
        for(String fileName : idfMapping.keySet()) {
            writer.write(fileName + "\t" + idfMapping.get(fileName) + "\n");
        }
        writer.close();
    }

    private void constructClusters(File directory) throws IOException, InvalidKeyException {
        File postingAddrFile = new File(directory, "postingAddr.txt");
        File outputFile = new File(directory, "index.txt");
        BlockedCompressedDictionary postingAddr = new BlockedCompressedDictionary(postingAddrFile);

        List<String> leaderNames = selectLeaders(fileIds.keySet());
        List<ClusterPosting> leaders = new ArrayList<>();
        for(String leaderName : leaderNames) {
            int postingOffset = postingAddr.get(leaderName);
            leaders.add(getPosting(outputFile, postingOffset));
        }

        Map<Integer, Cluster> clusters = new HashMap<>();
        for(Map.Entry<String, Integer> entry : fileIds.entrySet()) {
            int postingOffset = postingAddr.get(entry.getKey());
            ClusterPosting posting = getPosting(outputFile, postingOffset);
            ClusterPosting minLeader = Collections.min(leaders, Comparator.comparingDouble(l -> l.angleTo(posting)));
            clusters.putIfAbsent(minLeader.getFileId(), new Cluster(minLeader));
            clusters.get(minLeader.getFileId()).addFileId(entry.getValue());
        }

        File clusterFile = new File(directory, "clusters.txt");
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(clusterFile)));
        writeCodedInt(out, clusters.size());
        for(Map.Entry<Integer, Cluster> entry : clusters.entrySet())
            entry.getValue().writeCluster(out);
        out.close();
    }

    private List<String> selectLeaders(Set<String> set){
        int numToSelect = (int) Math.sqrt(set.size());
        List<String> copy = new ArrayList<>(set);
        Collections.shuffle(copy);
        return copy.subList(0, Math.min(numToSelect, copy.size()));
    }

    private ClusterPosting getPosting(File outputFile, int offset) throws IOException {
        PostingsList<ClusterPosting> postings = new PostingsList<>();
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(outputFile)));
        in.skipBytes(offset);
        postings.readPostingsList(in, ClusterPosting::new);
        in.close();
        return postings.getPostings().getFirst();
    }

    private ClusterPosting getNextPosting(DataInputStream in) throws IOException {
        PostingsList<ClusterPosting> list = new PostingsList<>();
        list.readPostingsList(in, ClusterPosting::new);
        return list.getPostings().getFirst();
    }
}
