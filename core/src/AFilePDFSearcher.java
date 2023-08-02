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
    protected SearchResult _searchResult;
    private String _word;
    protected boolean _stop = false, _pause = false;
    protected ConcurrentHashMap<Path, BasicFileAttributes> _bufferFiles = new ConcurrentHashMap<>();
    protected ConcurrentHashMap<Path, Boolean> _threadStatus = new ConcurrentHashMap<>();
    protected ConcurrentLinkedQueue<Path> _bufferDirectories = new ConcurrentLinkedQueue<>();
    protected long _bufferTotalFiles = 0;
    private final ExecutorService _threadPool;
    protected Cron _cron;

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
            _searchResult.IncreaseTotalFiles(_bufferTotalFiles);
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
            _threadStatus.put(startDir, false);
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
                        if (dir.equals(startDir)) {
                            _threadStatus.put(startDir, true);
                            getResult().searchIsFinished = _threadStatus.values().stream().allMatch(x -> x);
                            if (getResult().searchIsFinished) {
                                System.out.println("Search finish");
                            }
                        }
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
                    _bufferTotalFiles++;
                    _bufferFiles.put(file, attrs);
                    System.out.println("Buffered file " + file.toString());
                } else {
                    System.out.println("Handling file " + file.toString());
                    if (isNew) {
                        _searchResult.IncreaseTotalFiles();
                    }
                    onFoundPDFFile(file, attrs);
                }
            }
        }
    }

    protected void EnqueueFile(Path file, BasicFileAttributes attrs) {
        _bufferFiles.put(file, attrs);
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

    public void resume() throws IOException {
        if (_pause) {
            start();
        }
    }

    public SearchResult getResult() {
        return _searchResult;
    }

    public long getElapsedTime() {
        return _cron.getTime();
    }

    @Override
    public void close() throws Exception {
        _threadPool.close();
    }

    protected boolean searchWordInPDF(Path file) throws IOException {
        // Implement your code to search the word in the PDF here
        // You can use libraries like Apache PDFBox to extract text from the PDF and search the word.
        // Return true if word is found, false otherwise.
        // Sample code:

        try(PDDocument document = PDDocument.load(file.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            return text.toLowerCase().contains(" " + _word.toLowerCase() + " ");
        }
    }
}
