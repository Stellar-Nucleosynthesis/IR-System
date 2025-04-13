package realisations.zoned_index;

import utils.file_parsing_utils.FileFormatParser;
import utils.file_parsing_utils.FileFormatParserFactory;
import utils.file_parsing_utils.StemmingStringTokenizer;
import kernels.IndexerKernel;
import utils.spimi_index_constructor.SpimiIndexConstructor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ZonedIndexerKernel implements IndexerKernel {
    ZonedIndexerKernel(int threadId){
        this.threadId = threadId;
        indexConstructor = new SpimiIndexConstructor<>(new ZonedPostingFactory());
    }

    private final int threadId;
    private final List<String> fileNames = new ArrayList<>();
    private final SpimiIndexConstructor<ZonedPosting> indexConstructor;

    @Override
    public void analyze(File file) throws IOException {
        FileFormatParser reader = FileFormatParserFactory.getFileParser(file);
        int fileId = fileNames.size();
        fileNames.add(file.getAbsolutePath());
        ZonedPostingFactory factory = new ZonedPostingFactory();
        String line = reader.readLine();
        while (line != null) {
            for(String term : StemmingStringTokenizer.tokenize(line)) {
                ZonedPosting posting = factory.create(threadId, fileId);
                posting.addZone(reader.getCurrentZone());
                indexConstructor.addPosting(term, posting);
            }
            line = reader.readLine();
        }
        reader.close();
    }

    @Override
    public void writeResult(File directory) throws IOException {
        indexConstructor.constructIndex(directory);
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(directory, "fileNames.txt")));
        for(String fileName : fileNames) {
            writer.write(fileName + '\n');
        }
        writer.close();
    }
}
