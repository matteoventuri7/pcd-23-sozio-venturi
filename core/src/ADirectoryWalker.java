import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

/**
 * Provide a basic implementation of directory visitor, notifying each directory or file found.
 */
public abstract class ADirectoryWalker implements IDirectoryVisitor {
    private final Path _start;

    /**
     * @param start The initial path from start
     */
    public ADirectoryWalker(Path start){
        _start = start;
    }

    /**
     * Start to visit the directory
     * @throws IOException
     * @throws NullPointerException
     * @throws SecurityException
     */
    public void Start() throws IOException, NullPointerException, SecurityException {
        Objects.requireNonNull(_start);
        Files.walkFileTree(_start, new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FoundDirectory(dir, attrs);
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                return FoundFile(file, attrs);
            }
        });
    }

    /**
     * Method to implement to handle a new file
     * @param file The found file
     * @param attrs The file attributes
     * @return The result of computation
     */
    protected abstract FileVisitResult FoundFile(Path file, BasicFileAttributes attrs);

    /**
     * Method to implement to handle a new directory
     * @param dir The found directory
     * @param attrs The directory attributes
     * @return The result of computation
     */
    protected abstract FileVisitResult FoundDirectory(Path dir, BasicFileAttributes attrs);
}
