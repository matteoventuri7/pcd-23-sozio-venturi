import java.nio.file.Path;
import java.util.ArrayList;

public class SearchResult {
    private final ArrayList<Path> files = new ArrayList<>();
    private long totalFiles;

    boolean searchIsFinished;
    boolean computationIsFinished;

    public synchronized void addResult(Path file) {
        files.add(file);
    }

    public ArrayList<Path> getFiles(){
        return new ArrayList<>(files);
    }

    public synchronized void IncreaseTotalFiles() {
        totalFiles++;
    }

    void IncreaseTotalFiles(long count) {
        totalFiles += count;
    }

    public long getTotalFiles() {
        return totalFiles;
    }

    public boolean IsFinished(){
        return searchIsFinished && computationIsFinished;
    }
}
