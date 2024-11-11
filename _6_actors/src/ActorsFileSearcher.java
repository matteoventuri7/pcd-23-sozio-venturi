import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.Semaphore;

public class ActorsFileSearcher extends AFilePDFSearcher{
    protected int nComputedFiles=0;
    private ActorSystem<FoundFileMessage> fileFinderActor;
    private ActorSystem<BaseSearchMessage> positiveFileFoundActor;
    protected boolean finishAlreadyNotified = false;

    public ActorsFileSearcher(Path start, String word) {
        super(start, word);

        if(fileFinderActor == null) {
            nComputedFiles=0;
            fileFinderActor
                    = ActorSystem.create(FileSearchProtocolBehaviour.create(), "file-finder-actor");
            positiveFileFoundActor
                    = ActorSystem.create(PositiveFileSearchProtocolBehaviour.create(), "file-positive-finder-actor");
        }
    }

    @Override
    protected void onFoundPDFFile(Path file, BasicFileAttributes attrs) throws InterruptedException {
        CheckStartSearch();

        fileFinderActor.tell(new FoundFileMessage(this, positiveFileFoundActor, file, word));
    }

    @Override
    protected void onSearchIsFinished() throws InterruptedException {
        super.onSearchIsFinished();

        positiveFileFoundActor.tell(new FinishSearchMessage(this));
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
    public void close() throws Exception {
        super.close();
        fileFinderActor.terminate();
        positiveFileFoundActor.terminate();
    }
}

class FileSearchProtocolBehaviour extends AbstractBehavior<FoundFileMessage> {

    private FileSearchProtocolBehaviour(ActorContext<FoundFileMessage> context) {
        super(context);
    }

    @Override
    public Receive<FoundFileMessage> createReceive() {
        return newReceiveBuilder().onMessage(FoundFileMessage.class, this::onFileFound).build();
    }

    private Behavior<FoundFileMessage> onFileFound(FoundFileMessage msg) throws IOException {
        getContext().getLog().info("Found file " + msg.file + " from " + this.getContext().getSelf());

        var isPositive = AFilePDFSearcher.searchWordInPDF(msg.file, msg.word);

        msg.isPositive = isPositive;
        msg.fileFinderActor.tell(msg);

        return this;
    }

    public static Behavior<FoundFileMessage> create() {
        return Behaviors.setup(FileSearchProtocolBehaviour::new);
    }
}

class PositiveFileSearchProtocolBehaviour extends AbstractBehavior<BaseSearchMessage>{
    public PositiveFileSearchProtocolBehaviour(ActorContext<BaseSearchMessage> context) {
        super(context);
    }

    private Behavior<BaseSearchMessage> onPositiveFound(BaseSearchMessage baseMsg) {
        if(baseMsg instanceof FoundFileMessage foundMsg){

            getContext().getLog().info("File " + foundMsg.file + " from " + this.getContext().getSelf() + " contains text!");

            if(foundMsg.isPositive) {
                foundMsg.searcher.addResultAndNotify(foundMsg.file);
            }

            foundMsg.searcher.nComputedFiles++;
        }else{
            int i = 0;
        }

        baseMsg.searcher.notifyIfFinished();

        return this;
    }

    @Override
    public Receive<BaseSearchMessage> createReceive() {
        return newReceiveBuilder()
                .onMessage(BaseSearchMessage.class, this::onPositiveFound)
                .build();
    }

    public static Behavior<BaseSearchMessage> create() {
        return Behaviors.setup(PositiveFileSearchProtocolBehaviour::new);
    }
}