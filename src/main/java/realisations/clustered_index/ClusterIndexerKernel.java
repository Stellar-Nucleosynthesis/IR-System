package realisations.clustered_index;

import kernels.IndexerKernel;
import postings.PostingsList;
import utils.encoding_utils.BlockedCompressedDictionary;
import utils.encoding_utils.BlockedDictionaryCompressor;
import utils.file_parsing_utils.FileFormatParser;
import utils.file_parsing_utils.FileFormatParserFactory;
import utils.file_parsing_utils.StemmingStringTokenizer;
import utils.spimi_index_constructor.SpimiIndexConstructor;

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
                vector.addTerm(termId);
            }
            line = reader.readLine();
        }
        vector.toUnitVector();
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
            constructClusters(directory);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        writer.close();
    }

    private void constructClusters(File directory) throws IOException, InvalidKeyException {
        File postingAddrFile = new File(directory, "postingAddr.txt");
        File outputFile = new File(directory, "output.txt");
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
        DataInputStream in = new DataInputStream(new FileInputStream(outputFile));
        in.skipBytes(offset);
        postings.readPostingsList(in, ClusterPosting::new);
        in.close();
        return postings.getPostings().getFirst();
    }
}
