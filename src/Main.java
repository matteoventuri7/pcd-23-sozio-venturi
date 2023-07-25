import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws Exception {
        IWordSearcher s = new MultiThreadFileSearcher(Path.of("C://test"), "ciao");
        s.search();
        s.pause();
        System.out.println("MAIN-pause");
        Thread.sleep(3000);
        s.resume();
        System.out.println("MAIN-resume");
        s.close();
        PrintResult(s.getResult());
    }

    private static void PrintResult(SearchResult result) {
        System.out.println("IsPartial:" + result.isPartial());
        System.out.println("Total files:" + result.getTotalFiles());
        System.out.println("Found files:" + result.getFiles().size());
        for (Path file :
                result.getFiles()) {
            System.out.println(file.toString());
        }
    }
}

// per mac  var s = new MultiThreadFileSearcher(Path.of("/Users/diegosozio/Documents/PCD/test"), "ciao");
// per windows  var s = new MultiThreadFileSearcher(Path.of("C://test"), "ciao");