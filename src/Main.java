import java.nio.file.Path;


public class Main {
    public static void main(String[] args) throws Exception {
        String defaultFolderPath = "/Users/diegosozio/Documents/PCD/test";
        String defaultKeyword = "ciao";

        if (args.length == 0) {
            System.out.println("Usage: java Main <approach> ");
            System.out.println("Available approaches:");
            System.out.println("1 - MultiThreadFileSearcher");
            System.out.println("2 - VirtualThreadFileSearcher");
            System.out.println("3 - TaskFileSearcher");
            return;
        }

        // Utilizza i parametri della riga di comando per scegliere l'approccio
        String selectedApproach = args[0].toLowerCase();

        // Verifica se l'approccio selezionato è valido
        switch (selectedApproach) {
            case "1":
                break;
            case "2":
                break;
            case "3":
                break;
            default:
                throw new IllegalArgumentException("Invalid approach selected. Please choose an appropriate approach.");
        }

        // Utilizza i parametri della riga di comando se forniti, altrimenti utilizza quelli di default
        String folderPath = args.length > 1 ? args[1] : defaultFolderPath;
        String keyword = args.length > 2 ? args[2] : defaultKeyword;

        IWordSearcher s = null;

        // Scegli l'approccio in base al parametro selezionato
        switch (selectedApproach) {
            case "1":
                s = new MultiThreadFileSearcher(Path.of(folderPath), keyword);
                break;
            case "2":
                s = new VirtualThreadFileSearcher(Path.of(folderPath), keyword);
                break;
            case "3":
                //s = new TaskFileSearcher(Path.of(folderPath), keyword);
                break;
        }

        // Esegui l'approccio selezionato
        s.start();
        Thread.sleep(10);
        s.pause();
        System.out.println("MAIN-pause");
        Thread.sleep(3000);
        s.resume();
        System.out.println("MAIN-resume");
        s.close();
        PrintResult(s.getResult());
    }

    private static void PrintResult(SearchResult result) {
        System.out.println("Total files:" + result.getTotalFiles());
        System.out.println("Found files:" + result.getFiles().size());
        for (Path file : result.getFiles()) {
            System.out.println(file.toString());
        }
    }
}

// per mac  var s = new MultiThreadFileSearcher(Path.of("/Users/diegosozio/Documents/PCD/test"), "ciao");
// per windows  var s = new MultiThreadFileSearcher(Path.of("C://test"), "ciao");