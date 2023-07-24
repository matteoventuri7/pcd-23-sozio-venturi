import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Provide a basic implementation of directory visitor, notifying each directory or file found.
 */
public abstract class AFilePDFSearcher {
    private final Path _start;
    protected long _totalFiles = 0;
    protected boolean _stop = false, _pause = false;
    protected ArrayList<Pair<Path, BasicFileAttributes>> _bufferFiles = new ArrayList<>();
    protected ArrayList<Path> _bufferDirectories = new ArrayList<>();

    /**
     * @param start The initial path from start
     */
    public AFilePDFSearcher(Path start) {
        _start = start;
    }

    protected void reset() {
        if(!_pause){
            _bufferFiles.clear();
            _bufferDirectories.clear();
        }
        _stop = false;
        _pause = false;
    }

    /**
     * Start to visit the directory
     *
     * @throws IOException
     * @throws NullPointerException
     * @throws SecurityException
     */
    public void start() throws IOException, NullPointerException, SecurityException {
        reset();
        Objects.requireNonNull(_start);
        if (_bufferDirectories.isEmpty()) {
            _bufferDirectories.add(_start);
        }
        for(Pair<Path, BasicFileAttributes> dataFile : new ArrayList<>(_bufferFiles)){
            visitFileImpl(dataFile.item1(), dataFile.item2());
            _bufferFiles.remove(dataFile);
        }
        for (Path dir : new ArrayList<>(_bufferDirectories)) {
            _bufferDirectories.remove(dir);
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    visitFileImpl(file, attrs);
                    return super.visitFile(file, attrs);
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
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
        }
    }

    private void visitFileImpl(Path file, BasicFileAttributes attrs) {
        if (!_stop) {
            _totalFiles++;
            if (!attrs.isSymbolicLink() &&
                    attrs.isRegularFile() &&
                    getExtensionByStringHandling(file.getFileName().toString()).equals("pdf")) {
                if (_pause) {
                    _bufferFiles.add(new Pair<>(file, attrs));
                } else {
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

    private String getExtensionByStringHandling(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    protected void stop() {
        _stop = true;
    }

    protected void pause() {
        _pause = true;
    }

    protected void resume() throws IOException, NullPointerException, SecurityException {
        if (_pause) {
            start();
        }
    }
}
