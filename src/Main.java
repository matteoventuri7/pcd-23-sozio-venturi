import java.io.IOException;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        var s = new MultiThreadFileSearcher(Path.of("/Users/diegosozio/Documents/PCD/test"));
        try {
            var files = s.search("ciao");
            s.pause();
            s.resume();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    System.out.println("main closed");
    }
}

// per mac  var s = new MultiThreadFileSearcher(Path.of("/Users/diegosozio/Documents/PCD/test"));
// per windows  var s = new MultiThreadFileSearcher(Path.of("C://test"));