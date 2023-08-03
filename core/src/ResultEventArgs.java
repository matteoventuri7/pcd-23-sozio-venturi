import java.nio.file.Path;

public class ResultEventArgs {
    private Path file;
    private long totalResultsFiles;
    private long totalFiles;

    public ResultEventArgs(Path file, long totalFiles, long totalResultsFiles) {
        this.file = file;
        this.totalResultsFiles = totalResultsFiles;
        this.totalFiles=totalFiles;
    }

    public Path getFile() {
        return file;
    }

    public long getTotalResultsFiles() {
        return totalResultsFiles;
    }

    public long getTotalFiles() {
        return totalFiles;
    }
}
