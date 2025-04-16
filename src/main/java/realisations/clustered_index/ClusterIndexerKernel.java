package realisations.clustered_index;

import kernels.IndexerKernel;
import utils.encoding_utils.BlockedDictionaryCompressor;
import utils.file_parsing_utils.FileFormatParser;
import utils.file_parsing_utils.FileFormatParserFactory;
import utils.file_parsing_utils.StemmingStringTokenizer;

import java.io.*;
import java.util.*;

public class ClusterIndexerKernel implements IndexerKernel {
    public ClusterIndexerKernel(int threadId){
        this.threadId = threadId;
    }

    private final int threadId;
    private final Map<String, Integer> fileIds = new HashMap<>();
    private final Map<String, Integer> termIds = new HashMap<>();
    private final ClusterIndexConstructor indexConstructor = new ClusterIndexConstructor(DocumentVector::new);

    @Override
    public void analyze(File file) throws IOException {
        FileFormatParser reader = FileFormatParserFactory.getFileParser(file);
        int fileId = fileIds.size();
        fileIds.put(file.getAbsolutePath(), fileId);
        DocumentVector vector = new DocumentVector(threadId, fileId);
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
        writer.close();
    }
}
