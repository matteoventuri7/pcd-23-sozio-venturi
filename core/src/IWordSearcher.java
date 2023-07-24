import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public interface IWordSearcher {
    SearchResult search() throws IOException, NullPointerException, SecurityException;
}
