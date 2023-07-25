import java.nio.file.Path;
import java.util.ArrayList;

public class SearchResult {
    private boolean isPartial;
    private final ArrayList<Path> files = new ArrayList<>();
    private long totalFiles;

    public synchronized void addResult(Path file) {
        files.add(file);
    }

    public ArrayList<Path> getFiles(){
        return new ArrayList<>(files);
    }

    public void setResultPartial() {
        isPartial = true;
    }

    public boolean isPartial() {
        return isPartial;
    }

    public synchronized void IncreseTotalFiles() {
        totalFiles++;
    }

    void IncreseTotalFiles(long count) {
        totalFiles += count;
    }

    public long getTotalFiles() {
        return totalFiles;
    }
}
