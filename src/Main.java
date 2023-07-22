import java.io.IOException;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        var s = new MultiThreadFileSearcher(Path.of("C://test"));
        try {
            var files = s.search("ciao");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}