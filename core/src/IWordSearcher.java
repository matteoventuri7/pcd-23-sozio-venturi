import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public interface IWordSearcher extends AutoCloseable{
    void search() throws IOException, NullPointerException, SecurityException;
    SearchResult getResult();
    void stop();
    void pause();
    void resume() throws IOException;
}
