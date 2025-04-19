package realisations.simple_index;

import kernels.IndexerKernel;
import utils.file_parsing_utils.FileFormatParser;
import utils.file_parsing_utils.FileFormatParserFactory;
import utils.file_parsing_utils.StemmingStringTokenizer;
import utils.spimi_index_constructor.SpimiIndexConstructor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SimpleIndexerKernel implements IndexerKernel {
    public SimpleIndexerKernel(int threadId, int bufferSize){
        this.threadId = threadId;
        indexConstructor = new SpimiIndexConstructor<>(SimplePosting::new, bufferSize);
    }

    private final int threadId;
    private final List<String> fileNames = new ArrayList<>();
    private final SpimiIndexConstructor<SimplePosting> indexConstructor;

    @Override
    public void analyze(File file) throws IOException {
        FileFormatParser reader = FileFormatParserFactory.getFileParser(file);
        int fileId = fileNames.size();
        fileNames.add(file.getAbsolutePath());
        String line = reader.readLine();
        while (line != null) {
            for(String term : StemmingStringTokenizer.tokenize(line))
                indexConstructor.addPosting(term, new SimplePosting(threadId, fileId));
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
