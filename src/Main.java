// per mac  var s = new MultiThreadFileSearcher(Path.of("/Users/diegosozio/Documents/PCD/test"), "ciao");
// per windows  var s = new MultiThreadFileSearcher(Path.of("C://test"), "ciao");*/

import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        String folderPath;
        String keyword;

        // Richiedi all'utente di inserire il percorso della cartella
        System.out.println("Enter the folder path:");
        folderPath = scanner.nextLine();

        // Richiedi all'utente di inserire la parola da cercare
        System.out.println("Enter the keyword:");
        keyword = scanner.nextLine();

        System.out.println("Select the approach (1: Multithreaded, 2: Virtual Thread, 3: Task Java, 4: Events):");
        int approachChoice = scanner.nextInt();
        scanner.nextLine(); // Consume the remaining newline character

        final IWordSearcher s;
        Path file = Path.of(folderPath);
        switch (approachChoice) {
            case 1:
                s = new MultiThreadFileSearcher(file, keyword);
                break;
            case 2:
                s = new VirtualThreadFileSearcher(file, keyword);
                break;
            case 3:
                s = new TaskFileSearcher(file, keyword);
                break;
            case 4:
                s = new EventsFileSearcher(file, keyword);
                break;
            default:
                System.out.println("Invalid approach choice. Exiting...");
                return;
        }

        s.register(new IEventsRegistrable() {
            @Override
            public void onNewResultFile(ResultEventArgs ev) {
                System.out.println("Found: " + ev.getFile().toString());
            }

            @Override
            public void onNewFoundFile(long totalFiles) {
                System.out.println("Total files processed: " + totalFiles);
            }

            @Override
            public void onFinish(SearchResult result) {
                System.out.println("Total files found: " + result.getTotalFoundFiles());
                System.out.println("Total files processed: " + result.getTotalFiles());
                System.out.println("Computing Time: " + result.getElapsedTime() + " ms");

                try {
                    s.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                System.out.println("Program end");
            }
        });

        System.out.println("Starting the search...");
        s.start();
    }
}

