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
public abstract class AFilePDFWalker {
    private final Path _start;

    /**
     * @param start The initial path from start
     */
    public AFilePDFWalker(Path start){
        _start = start;
    }

    /**
     * Start to visit the directory
     * @throws IOException
     * @throws NullPointerException
     * @throws SecurityException
     */
    public void start() throws IOException, NullPointerException, SecurityException {
        Objects.requireNonNull(_start);
        Files.walkFileTree(_start, new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if(!attrs.isSymbolicLink() &&
                        attrs.isRegularFile() &&
                        getExtensionByStringHandling(file.getFileName().toString()).equals("pdf")){
                    foundPDFFile(file);
                }
                return super.visitFile(file, attrs);
            }
        });
    }

    /**
     * Method to implement to handle a new file
     * @param file The found file
     */
    protected abstract void foundPDFFile(Path file);

    private String getExtensionByStringHandling(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

}
