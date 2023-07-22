import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public interface IWordSearcher {
    ArrayList<Path> search(String word) throws IOException;
}
