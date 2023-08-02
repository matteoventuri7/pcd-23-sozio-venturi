import java.nio.file.Path;

public class ResultEventArgs {
    private Path file;
    private long totalResultsFiles;

    public ResultEventArgs(Path file, long totalResultsFiles) {
        this.file = file;
        this.totalResultsFiles = totalResultsFiles;
    }

    public Path getFile() {
        return file;
    }

    public long getTotalResultsFiles() {
        return totalResultsFiles;
    }
}
