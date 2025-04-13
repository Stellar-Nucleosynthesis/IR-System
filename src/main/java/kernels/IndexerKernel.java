package kernels;

import java.io.File;
import java.io.IOException;

public interface IndexerKernel {
    void analyze(File file) throws IOException;
    void writeResult(File directory) throws IOException;
}
