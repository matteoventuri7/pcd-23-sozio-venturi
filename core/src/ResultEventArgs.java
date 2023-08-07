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

    /**
     * Get the file containing the word
     * @return
     */
    public Path getFile() {
        return file;
    }

    /**
     * Get the count files containing the word
     * @return
     */
    public long getTotalResultsFiles() {
        return totalResultsFiles;
    }

    /**
     * Get the count files were found
     * @return
     */
    public long getTotalFiles() {
        return totalFiles;
    }
}
