import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Provide a basic implementation of directory visitor, notifying each directory or file found.
 */
public abstract class AFilePDFSearcher
        implements IWordSearcher {
    private final Path _start;
    private SearchResult _searchResult;
    private String _word;
    private boolean _stop = false, _pause = false;
    /**
     * Used to store found files while program is suspended
     */
    private ConcurrentHashMap<Path, BasicFileAttributes> _bufferFiles = new ConcurrentHashMap<>();
    /**
     * Used to store directories while program is suspended
     */
    private ConcurrentLinkedQueue<Path> _bufferDirectories = new ConcurrentLinkedQueue<>();
    private final ExecutorService _threadPool;
    /**
     * Mark the computation time
     */
    private Cron _cron;
    /**
     * It's the observer interested on receive some events.
     */
    private IEventsRegistrable _guiRegistrable;

    /**
     * @param start The initial path from start
     */
    public AFilePDFSearcher(Path start, String word) {
        _start = start;
        _word = word;
        _threadPool = Executors.newSingleThreadExecutor();
        _searchResult = new SearchResult();
        _cron = new Cron();
    }

    protected void resetLists() {
        // clear lists when we start again after a pause
        _bufferFiles.clear();
        _bufferDirectories.clear();
    }

    /**
     * Start to visit the directory
     *
     * @throws NullPointerException
     * @throws SecurityException
     */
    public void start() {
        _stop = false;

        if (_pause) {
            _pause = false;
            // resuming files
            for (Path pathFile : _bufferFiles.keySet()) {
                visitFileImpl(pathFile, _bufferFiles.get(pathFile), false);
                _bufferFiles.remove(pathFile);
            }

            // resuming directories
            for (Path dir : _bufferDirectories) {
                _bufferDirectories.remove(dir);
                startFileWalker(dir);
            }
            resetLists();
        } else {
            _cron.start();
            _searchResult = new SearchResult();
            // fresh start or start again after a stop
            resetLists();
            Objects.requireNonNull(_start);
            startFileWalker(_start);
        }
    }

    private void startFileWalker(Path startDir) {
        _threadPool.execute(() -> {
            try {
                Files.walkFileTree(startDir, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        visitFileImpl(file, attrs, true);
                        return super.visitFile(file, attrs);
                    }

                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        System.out.println("Visit directory - Stop:" + _stop + ";Pause:" + _pause);
                        if (_stop) return FileVisitResult.TERMINATE;
                        else if (_pause) {
                            _bufferDirectories.add(dir);
                            return FileVisitResult.TERMINATE;
                        }
                        return super.preVisitDirectory(dir, attrs);
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        if (_stop) return FileVisitResult.TERMINATE;
                        return super.postVisitDirectory(dir, exc);
                    }
                });
            } catch (Exception e) {
                // some errors here but prevent block
                System.out.println(e.toString());
            }
        });
    }

    private void visitFileImpl(Path file, BasicFileAttributes attrs, boolean isNew) {
        System.out.println("Visit file - Stop:" + _stop + ";Pause:" + _pause);
        if (!_stop) {
            if (!attrs.isSymbolicLink() &&
                    attrs.isRegularFile() &&
                    getExtensionFile(file.getFileName().toString()).equals("pdf")) {
                if (_pause) {
                    _bufferFiles.put(file, attrs);
                    System.out.println("Buffered file " + file.toString());
                } else {
                    System.out.println("Handling file " + file.toString());
                    if (isNew) {
                        CountNewFileAndNotify();
                    }
                    onFoundPDFFile(file, attrs);
                }
            }
        }
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
        _stop = true;
    }

    public void pause() {
        _pause = true;
    }

    public void resume() {
        if (_pause) {
            start();
        }
    }

    public SearchResult getResult() {
        return _searchResult;
    }

    protected long getElapsedTime() {
        return _cron.getTime();
    }

    @Override
    public void close() throws Exception {
        _threadPool.close();
    }

    /**
     * Contains the common logic to find out if the file contains the word
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
            boolean contains = text.toLowerCase().contains(_word.toLowerCase());
            return contains;
        }
    }

    public void register(IEventsRegistrable registrable) {
        _guiRegistrable = registrable;
    }

    protected synchronized void notifyFinish() {
        _searchResult.setElapsedTime(getElapsedTime());
        if (_guiRegistrable != null)
            _guiRegistrable.onFinish(_searchResult);
    }

    /**
     * This method implement the common share logic
     *
     * @param file
     * @param attrs
     * @return true if the file is worked, false otherwise (i.e. suspended)
     */
    protected boolean searchWordInsideFile(Path file, BasicFileAttributes attrs) {
        if (_pause) {
            _bufferFiles.put(file, attrs);
        } else if (!_stop) {
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
     * @param file
     */
    private synchronized void AddResultAndNotify(Path file){
        _searchResult.addResult(file);
        if (_guiRegistrable != null) {
            _guiRegistrable.onNewResultFile(new ResultEventArgs(file, _searchResult.getTotalFiles(), _searchResult.getTotalFoundFiles()));
        }
    }

    /**
     * Increase the found file counter and notify the observer
     */
    private synchronized void CountNewFileAndNotify() {
        _searchResult.IncreaseTotalFiles();
        if (_guiRegistrable != null) {
            _guiRegistrable.onNewFoundFile(_searchResult.getTotalFiles());
        }
    }
}
