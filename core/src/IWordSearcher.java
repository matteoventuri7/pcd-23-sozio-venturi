import java.io.IOException;

public interface IWordSearcher extends AutoCloseable{
    /**
     * Start to search the word in the files
     */
    void start();

    /**
     * Retrieve the final result
     * @return
     */
    SearchResult getResult();

    /**
     * Stop the research
     */
    void stop();

    /**
     * Suspend the research
     */
    void pause();

    /**
     * Resume the research
     */
    void resume();

    /**
     * Register an observer to the research flow (i.e. the GUI)
     * @param registrable
     */
    void register(IEventsRegistrable registrable);
}
