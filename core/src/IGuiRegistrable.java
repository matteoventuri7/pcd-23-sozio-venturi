import java.nio.file.Path;

public interface IGuiRegistrable {
    public void onNewResultFile(ResultEventArgs ev);
    public void onNewFoundFile(long totalFiles);
    public void onFinish(SearchResult result);
}
