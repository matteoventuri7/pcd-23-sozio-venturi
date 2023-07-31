import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class MyFileSearcher extends WordSearchPDFFileSearcher {
    public MyFileSearcher(Path start, String word) {
        super(start, word);
    }

    @Override
    protected void onFoundPDFFile(Path file, BasicFileAttributes attrs) {
        // Implementa qui la logica per la gestione dei file PDF trovati
        System.out.println("Found PDF file: " + file.toString());
    }
}
