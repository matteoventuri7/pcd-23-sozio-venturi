import java.io.IOException;
import java.nio.file.Path;

public class MyFileSearcher extends WordSearchPDFFileSearcher {
    public MyFileSearcher(Path start, String word) {
        super(start, word);
    }

    @Override
    protected void onFoundPDFFile(Path file) {
        // Implementa qui la logica per la gestione dei file PDF trovati
        System.out.println("Found PDF file: " + file.toString());
    }

    @Override
    protected void onSearchFilesFinished() {
        // Implementa qui la logica da eseguire quando la ricerca è finita
        System.out.println("Search finished!");
    }

    @Override
    public void search() throws IOException, NullPointerException, SecurityException {

    }

    @Override
    public boolean isFinished() {
        // Implementa qui la logica per verificare se la ricerca è finita
        // Ritorna true se la ricerca è finita, altrimenti ritorna false
        // Ad esempio, potresti controllare se tutti i file e le directory sono stati processati.
        return true; // Per ora, ritorniamo semplicemente true.
    }
}
