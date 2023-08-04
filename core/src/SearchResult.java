import java.nio.file.Path;
import java.util.ArrayList;

public class SearchResult {
    private final ArrayList<Path> files = new ArrayList<>();
    private long totalFiles;
    private long elapsedTime;
    private boolean searchIsFinished;
    private boolean computationIsFinished;

    protected void addResult(Path file) {
        files.add(file);
    }

    public ArrayList<Path> getFiles(){
        return new ArrayList<>(files);
    }

    protected void IncreaseTotalFiles() {
        totalFiles++;
    }

    protected void IncreaseTotalFiles(long count) {
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

    protected void setElapsedTime(long et){
        elapsedTime = et;
    }

    protected void setComputationFinished() {
        computationIsFinished = true;
    }

    protected void setSearchIsFinished() {
        searchIsFinished = true;
    }
}
