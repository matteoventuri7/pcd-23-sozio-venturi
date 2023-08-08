import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.*;

/**
 * Provide a basic implementation of directory visitor, notifying each directory or file found.
 */
public abstract class AFilePDFSearcher
        implements IWordSearcher {
    private final Path start;
    private SearchResult searchResult;
    private String word;
    private boolean stop = false, pause = false, researchIsfinished = false;
    private Semaphore fileWalkerSemaphore = new Semaphore(1, true);
    private final ExecutorService threadPool;
    /**
     * Mark the computation time
     */
    private Cron cron;
    /**
     * It's the observer interested on receive some events.
     */
    private IEventsRegistrable guiRegistrable;

    /**
     * @param start The initial path from start
     */
    public AFilePDFSearcher(Path start, String word) {
        this.start = start;
        this.word = word;
        threadPool = Executors.newSingleThreadExecutor();
        searchResult = new SearchResult();
        cron = new Cron();
    }

    protected boolean isPaused(){
        return pause;
    }

    protected boolean isResearchFinished() {
        return researchIsfinished;
    }

    /**
     * Start to visit the directory
     *
     * @throws NullPointerException
     * @throws SecurityException
     */
    public void start() {
        stop = false;

        researchIsfinished = false;
        cron.start();
        searchResult = new SearchResult();

        Objects.requireNonNull(start);

        startFileWalker(start);
    }

    private void startFileWalker(Path startDir) {
        threadPool.execute(() -> {
            try {
                Files.walkFileTree(startDir, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        try {
                            if (!stop) {
                                try {
                                    fileWalkerSemaphore.acquire();

                                    if (!attrs.isSymbolicLink() &&
                                            attrs.isRegularFile() &&
                                            getExtensionFile(file.getFileName().toString()).equals("pdf")) {
                                        Thread.sleep(2000);
                                        System.out.println("Handling file " + file.toString());
                                        CountNewFileAndNotify();
                                        onFoundPDFFile(file, attrs);
                                    }
                                } finally {
                                    fileWalkerSemaphore.release();
                                }
                            } else {
                                return FileVisitResult.TERMINATE;
                            }
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        return super.visitFile(file, attrs);
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        if (stop || dir.equals(startDir)) {
                            researchIsfinished = true;
                            return FileVisitResult.TERMINATE;
                        }
                        return super.postVisitDirectory(dir, exc);
                    }
                });
            } catch (Exception e) {
                // some errors here but prevent block
                System.out.println(e.toString());
            }
        });
    }

    /**
     * Method to implement to handle a new file
     *
     * @param file The found file
     */
    protected abstract void onFoundPDFFile(Path file, BasicFileAttributes attrs);

    private String getExtensionFile(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    public void stop() {
        stop = true;
    }

    public void pause() {
        try {
            fileWalkerSemaphore.acquire();
            pause = true;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void resume() {
        fileWalkerSemaphore.release();
        pause = false;
    }

    public SearchResult getResult() {
        return searchResult;
    }

    protected long getElapsedTime() {
        return cron.getTime();
    }

    @Override
    public void close() throws Exception {
        stop = true;
        fileWalkerSemaphore.release();

        if(threadPool != null) {
            threadPool.shutdown();
            threadPool.awaitTermination(1, TimeUnit.SECONDS);
            threadPool.close();
        }
    }

    /**
     * Contains the common logic to find out if the file contains the word
     *
     * @param file
     * @return
     * @throws IOException
     */
    protected boolean searchWordInPDF(Path file) throws IOException {
        // Implement your code to search the word in the PDF here
        // You can use libraries like Apache PDFBox to extract text from the PDF and search the word.
        // Return true if word is found, false otherwise.
        // Sample code:

        try (PDDocument document = PDDocument.load(file.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            boolean contains = text.toLowerCase().contains(word.toLowerCase());
            Thread.sleep(200);
            return contains;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void register(IEventsRegistrable registrable) {
        guiRegistrable = registrable;
    }

    protected synchronized void notifyFinish() {
        searchResult.setElapsedTime(getElapsedTime());
        if (guiRegistrable != null)
            guiRegistrable.onFinish(searchResult);
    }

    /**
     * This method implement the common share logic
     *
     * @param file
     * @param attrs
     * @return true if the file is worked, false otherwise (i.e. suspended)
     */
    protected boolean searchWordInsideFile(Path file, BasicFileAttributes attrs) throws InterruptedException {
        if (!stop) {
            // Search the word in the PDF
            boolean wordFoundInPDF = false;
            try {
                wordFoundInPDF = searchWordInPDF(file);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (wordFoundInPDF) {
                AddResultAndNotify(file);
            }

            return true;
        }

        return false;
    }

    /**
     * Add the file that contains the word in the results and notify the observer
     *
     * @param file
     */
    private synchronized void AddResultAndNotify(Path file) {
        searchResult.addResult(file);
        if (guiRegistrable != null) {
            guiRegistrable.onNewResultFile(new ResultEventArgs(file, searchResult.getTotalFiles(), searchResult.getTotalFoundFiles()));
        }
    }

    /**
     * Increase the found file counter and notify the observer
     */
    private synchronized void CountNewFileAndNotify() {
        searchResult.IncreaseTotalFiles();
        if (guiRegistrable != null) {
            guiRegistrable.onNewFoundFile(searchResult.getTotalFiles());
        }
    }
}
