import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

public class EventsFileSearcher extends AFilePDFSearcher {
    protected final static String topicName = "pdf-search-result";
    private Vertx vertx;
    private int nComputedFiles=0;
    private boolean finishAlreadyNotified = false;
    private Semaphore finishSem = new Semaphore(1);

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
                    eb.<String>consumer(topicName, message -> {
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

        vertx.deployVerticle(new AbstractVerticle(){
            public void start() throws InterruptedException, IOException {
                var isPositive = AFilePDFSearcher.searchWordInPDF(file, word);
                if(isPositive) {
                    EventBus eb = this.getVertx().eventBus();
                    eb.publish(EventsFileSearcher.topicName, file.toString());
                }
            }
        });
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