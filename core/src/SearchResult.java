import java.nio.file.Path;
import java.util.ArrayList;

public class SearchResult {
    private final ArrayList<Path> files = new ArrayList<>();
    private long totalFiles;
    private long elapsedTime;
    private boolean searchIsFinished;
    private boolean computationIsFinished;

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

    public long getElapsedTime() {
        return elapsedTime;
    }

    void setElapsedTime(long et){
        elapsedTime = et;
    }

    public void setComputationFinished() {
        computationIsFinished = true;
    }

    public void setSearchIsFinished() {
        searchIsFinished = true;
    }
}
