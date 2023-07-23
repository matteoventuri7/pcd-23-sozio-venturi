import java.nio.file.Path;
import java.util.ArrayList;

public class SearchResult {
    private final boolean isPartial;
    private final ArrayList<Path> files;
    private final long totalFiles;

    public SearchResult(boolean isPartial, ArrayList<Path> files, long totalFiles) {
        this.isPartial = isPartial;
        this.files = new ArrayList<>(files);
        this.totalFiles=totalFiles;
    }
}
