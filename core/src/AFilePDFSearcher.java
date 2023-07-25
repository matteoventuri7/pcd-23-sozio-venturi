import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Provide a basic implementation of directory visitor, notifying each directory or file found.
 */
public abstract class AFilePDFSearcher
        implements IWordSearcher {
    private final Path _start;
    protected SearchResult _serchResult;
    private String _word;
    protected boolean _stop = false, _pause = false;
    protected HashSet<Pair<Path, BasicFileAttributes>> _bufferFiles = new HashSet<>();
    protected HashSet<Path> _bufferDirectories = new HashSet<>();
    protected long _bufferTotalFiles = 0;
    private ExecutorService _threadPool;

    /**
     * @param start The initial path from start
     */
    public AFilePDFSearcher(Path start, String word) {
        _start = start;
        _word = word;
        _threadPool = Executors.newCachedThreadPool();
    }

    protected void reset() {
        // clear lists when we start again after a pause
        _bufferFiles.clear();
        _bufferDirectories.clear();
        _stop = false;
        _pause = false;
        _serchResult = new SearchResult();
    }

    /**
     * Start to visit the directory
     *
     * @throws IOException
     * @throws NullPointerException
     * @throws SecurityException
     */
    public void start() throws IOException, NullPointerException, SecurityException {
        if (_pause) {
            // resuming files
            _serchResult.IncreseTotalFiles(_bufferTotalFiles);
            for (Pair<Path, BasicFileAttributes> dataFile : new ArrayList<>(_bufferFiles)) {
                visitFileImpl(dataFile.item1(), dataFile.item2());
                _bufferFiles.remove(dataFile);
            }
            // resuming directories
            for (Path dir : new ArrayList<>(_bufferDirectories)) {
                _bufferDirectories.remove(dir);
                startFileWalker(dir);
            }
            reset();
        } else {
            // fresh start or start again after a stop
            reset();
            Objects.requireNonNull(_start);
            startFileWalker(_start);
        }
    }

    private void startFileWalker(Path startDir) throws IOException {
        _threadPool.execute(() -> {
            try {
                Files.walkFileTree(startDir, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        visitFileImpl(file, attrs);
                        return super.visitFile(file, attrs);
                    }

                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        System.out.println("Visit directory - Stop:"+_stop+";Pause:"+_pause);
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
            } catch (Exception e) {}
        });
    }

    private void visitFileImpl(Path file, BasicFileAttributes attrs) {
        System.out.println("Visit file - Stop:"+_stop+";Pause:"+_pause);
        if (!_stop) {
            if (_pause) {
                _bufferTotalFiles++;
            } else {
                _serchResult.IncreseTotalFiles();
            }
            if (!attrs.isSymbolicLink() &&
                    attrs.isRegularFile() &&
                    getExtensionFile(file.getFileName().toString()).equals("pdf")) {
                if (_pause) {
                    _bufferFiles.add(new Pair<>(file, attrs));
                    System.out.println("Buffered file " + file.toString());
                } else {
                    System.out.println("Handling file " + file.toString());
                    foundPDFFile(file);
                }
            }
        }
    }

    /**
     * Method to implement to handle a new file
     *
     * @param file The found file
     */
    protected abstract void foundPDFFile(Path file);

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
        return _serchResult;
    }

    @Override
    public void close() throws Exception {
        _threadPool.close();
    }
}
