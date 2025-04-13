package retrieval_system;

import java.io.File;
import java.util.List;

public interface Indexer {
    void analyze(File outputDir, List<File> target) throws InterruptedException;
}
