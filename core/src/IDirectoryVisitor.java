import java.io.IOException;

public interface IDirectoryVisitor {
    /**
     * Start to visit the directory
     * @throws IOException
     * @throws NullPointerException
     * @throws SecurityException
     */
    void Start() throws IOException, NullPointerException, SecurityException;
}
