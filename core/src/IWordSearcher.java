import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public interface IWordSearcher extends AutoCloseable{
    void start();
    SearchResult getResult();
    void stop();
    void pause();
    void resume() throws IOException;
    long getElapsedTime();
}
