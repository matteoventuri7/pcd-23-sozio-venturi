public interface IEventsRegistrable {
    /**
     * Triggered when is found a file containing the word
     * @param ev
     */
    public void onNewResultFile(ResultEventArgs ev);

    /**
     * Triggered when a new file is found
     * @param totalFiles
     */
    public void onNewFoundFile(long totalFiles);

    /**
     * Triggered when all files were processed
     * @param result
     */
    public void onFinish(SearchResult result);
}
