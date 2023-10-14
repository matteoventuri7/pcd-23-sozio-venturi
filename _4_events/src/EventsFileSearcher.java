import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.TimeUnit;

public class EventsFileSearcher extends AFilePDFSearcher {
    protected final static String topicName = "pdf-search-result";
    private final Vertx vertx;
    private int nComputedFiles=0;

    public EventsFileSearcher(Path start, String word) {
        super(start, word);
        vertx = Vertx.vertx();
        vertx.deployVerticle(new AbstractVerticle(){
            public void start() {
                EventBus eb = this.getVertx().eventBus();
                eb.consumer(topicName, message -> {
                    Path positiveFile = (Path) message.body();
                    AddResultAndNotify(positiveFile);
                    notifyIfFinished();
                });
            }
        });
    }

    @Override
    protected void onFoundPDFFile(Path file, BasicFileAttributes attrs) {
        vertx.deployVerticle(new SearcherAgent(file, word));
    }

    protected void notifyIfFinished() {
        nComputedFiles++;

        if (isResearchFinished() && isFinished()) {
            notifyFinish();
        }
    }

    private boolean isFinished() {
        return !isPaused() && nComputedFiles == getResult().getTotalFiles();
    }

    @Override
    public void close() throws Exception {
        super.close();
        vertx.close();
    }
}

class SearcherAgent extends AbstractVerticle {
    private final Path file;
    private final String word;

    public SearcherAgent(Path file, String word) {
        this.file=file;
        this.word=word;
    }

    public void start() throws IOException {
        var isPositive = AFilePDFSearcher.searchWordInPDF(file, word);
        if(isPositive) {
            EventBus eb = this.getVertx().eventBus();
            eb.publish(EventsFileSearcher.topicName, file);
        }
    }
}