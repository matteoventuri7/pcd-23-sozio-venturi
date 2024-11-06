import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.nio.file.*;
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
    protected final String word;
    private boolean stop = false, pause = false, researchIsfinished = false;
    private final Semaphore fileWalkerSemaphore = new Semaphore(1, true);
    private final Semaphore searcherSemaphore = new Semaphore(1, true);
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
     * The caller is blocked if program was suspended, otherwise the caller continue.
     * @throws InterruptedException
     */
    protected void CheckStartSearch() throws InterruptedException {
        searcherSemaphore.acquire();
        searcherSemaphore.release();
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
                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                        if (exc instanceof AccessDeniedException) {
                            return FileVisitResult.SKIP_SUBTREE;
                        }

                        return super.visitFileFailed(file, exc);
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        System.out.println(file.toString());
                        try {
                            if (!stop) {
                                try {
                                    fileWalkerSemaphore.acquire();

                                    if (!attrs.isSymbolicLink() &&
                                            attrs.isRegularFile() &&
                                            getExtensionFile(file.getFileName().toString()).equals("pdf")) {
                                        //Thread.sleep(1000);
                                        System.out.println("Handling file " + file.toString());
                                        CountNewFileAndNotify();
                                        onFoundPDFFile(file, attrs);
                                    }
                                } catch (Exception ex) {
                                    throw new RuntimeException(ex);
                                } finally {
                                    fileWalkerSemaphore.release();
                                }
                            } else {
                                return FileVisitResult.TERMINATE;
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        return super.visitFile(file, attrs);
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        if (stop || dir.equals(startDir)) {
                            onSearchIsFinished();
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

    protected void onSearchIsFinished() {
        researchIsfinished = true;
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
            searcherSemaphore.acquire();
            pause = true;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void resume() {
        pause = false;
        searcherSemaphore.release();
        fileWalkerSemaphore.release();
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
        searcherSemaphore.release();

        if(threadPool != null) {
            threadPool.shutdown();
            threadPool.awaitTermination(1, TimeUnit.SECONDS);
        }
    }

    /**
     * Contains the common logic to find out if the file contains the word
     *
     * @param file
     * @return
     * @throws IOException
     */
    protected static boolean searchWordInPDF(Path file, String word) throws IOException {
        try (PDDocument document = PDDocument.load(file.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            boolean contains = text.toLowerCase().contains(word.toLowerCase());
            //Thread.sleep(1000);
            return contains;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void register(IEventsRegistrable registrable) {
        guiRegistrable = registrable;
    }

    protected void notifyFinish() {
        searchResult.setElapsedTime(getElapsedTime());
        if (guiRegistrable != null)
            guiRegistrable.onFinish(searchResult);
    }

    /**
     * This method implement the common share logic
     *
     * @param file
     * @param attrs
     * @return true if the file is positive, false otherwise and null if search is suspended
     */
    protected Optional<Boolean> searchWordInsideFile(Path file, BasicFileAttributes attrs) throws InterruptedException {
        if(stop){
            return Optional.empty();
        }

        // Search the word in the PDF
        boolean wordFoundInPDF = false;
        try {
            //Thread.sleep(1000);
            wordFoundInPDF = searchWordInPDF(file, word);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.of(wordFoundInPDF);
    }

    /**
     * Add the file that contains the word in the results and notify the observer
     *
     * @param file
     */
    protected void AddResultAndNotify(Path file) {
        searchResult.addResult(file);
        if (guiRegistrable != null) {
            guiRegistrable.onNewResultFile(new ResultEventArgs(file, searchResult.getTotalFiles(), searchResult.getTotalFoundFiles()));
        }
    }

    /**
     * Increase the found file counter and notify the observer
     */
    private void CountNewFileAndNotify() {
        searchResult.IncreaseTotalFiles();
        if (guiRegistrable != null) {
            guiRegistrable.onNewFoundFile(searchResult.getTotalFiles());
        }
    }
}
