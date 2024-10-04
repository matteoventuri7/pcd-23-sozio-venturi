import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.Semaphore;

public class EventsFileSearcher extends AFilePDFSearcher {
    protected final static String topicNameResults = "pdf-search-result";
    protected final static String topicNameSearcher = "pdf-search-request";
    private Vertx vertx;
    private int nComputedFiles=0;
    private boolean finishAlreadyNotified = false;
    private Semaphore finishSem = new Semaphore(1);
    private Future<String> _futSearcher;
    private EventBus _eventBus;

    public EventsFileSearcher(Path start, String word) {
        super(start, word);
    }

    @Override
    public void start() {
        nComputedFiles=0;

        if(vertx == null){
            vertx = Vertx.vertx();
            vertx.deployVerticle(new AbstractVerticle(){
                public void start() {
                    EventBus eb = this.getVertx().eventBus();
                    eb.<String>consumer(topicNameResults, message -> {
                        String positiveFile = message.body();
                        AddResultAndNotify(Path.of(positiveFile));
                        try {
                            finishSem.acquire();
                            notifyIfFinished();
                            finishSem.release();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                    });
                }
            });

            _futSearcher = vertx.deployVerticle(new SearcherVerticle());

            _eventBus = vertx.eventBus();
        }

        super.start();
    }

    @Override
    protected void onFoundPDFFile(Path file, BasicFileAttributes attrs) {
        try {
            CheckStartSearch();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        nComputedFiles++;

        JsonObject message = new JsonObject()
                .put("file", file.toString())
                .put("word", word);

        var json = message.encode();

        _eventBus.publish(topicNameSearcher, json);
    }

    protected void notifyIfFinished() {
        if (!finishAlreadyNotified && isResearchFinished() && isFinished()) {
            finishAlreadyNotified = true;
            notifyFinish();
        }
    }

    private boolean isFinished() {
        return !isPaused() && nComputedFiles == getResult().getTotalFiles();
    }

    @Override
    protected void onSearchIsFinished() {
        super.onSearchIsFinished();
        try {
            finishSem.acquire();
            notifyIfFinished();
            finishSem.release();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        super.close();
        if(vertx != null) {
            vertx.close();
        }
    }
}

class SearcherVerticle extends AbstractVerticle {
    @Override
    public void start() {
        this.getVertx().eventBus().consumer(EventsFileSearcher.topicNameSearcher, message -> {
            var json = message.body().toString();
            JsonObject request = new JsonObject(json);

            var file = Path.of(request.getString("file"));
            var word = request.getString("word");

            this.getVertx().getOrCreateContext().runOnContext(_ -> {
                try {
                    var isPositive = AFilePDFSearcher.searchWordInPDF(file, word);
                    if(isPositive) {
                        this.getVertx().eventBus().publish(EventsFileSearcher.topicNameResults, file.toString());
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }
}