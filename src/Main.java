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

        System.out.println("Select the approach (1: Multithreaded, 2: Virtual Thread, 3: Task Java):");
        int approachChoice = scanner.nextInt();
        scanner.nextLine(); // Consume the remaining newline character

        final IWordSearcher s;
        switch (approachChoice) {
            case 1:
                s = new MultiThreadFileSearcher(Path.of(folderPath), keyword);
                break;
            case 2:
                s = new VirtualThreadFileSearcher(Path.of(folderPath), keyword);
                break;
            case 3:
                // Implement the Task Java approach here if needed
                s = null;
                break;
            default:
                System.out.println("Invalid approach choice. Exiting...");
                return;
        }

        if (s != null) {
            s.register(new IGuiRegistrable() {
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
                    System.out.println("Total files found: " + result.getFiles().size());
                    System.out.println("Total files processed: " + result.getTotalFiles());
                    System.out.println("Computing Time: " + s.getElapsedTime() + " ms");
                }
            });

            System.out.println("Starting the search...");
            s.start();

            // Pause after 10 milliseconds
            sleep(10);

            System.out.println("Pausing the search...");
            s.pause();

            // Resume after 3000 milliseconds (3 seconds)
            sleep(3000);

            System.out.println("Resuming the search...");
            s.resume();

            System.out.println("Closing the search...");
            try {
                s.close();
            } catch (java.io.IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

