import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.Semaphore;

public class ActorsFileSearcher extends AFilePDFSearcher{
    protected int nComputedFiles=0;
    private ActorSystem<FileSearchProtocol.FoundFileMessage> fileFinderActor;
    private ActorSystem<FileSearchProtocol.FoundFileMessage> positiveFileFoundActor;
    protected Semaphore finishSem = new Semaphore(1);
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
    protected void onFoundPDFFile(Path file, BasicFileAttributes attrs) {
        try {
            CheckStartSearch();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        fileFinderActor.tell(new FileSearchProtocol.FoundFileMessage(this, positiveFileFoundActor, file, word));
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

class FileSearchProtocol {
    public static class FoundFileMessage {
        public final ActorRef<FileSearchProtocol.FoundFileMessage> fileFinderActor;
        public final Path file;
        public final String word;
        public final ActorsFileSearcher searcher;
        public boolean isPositive;

        public FoundFileMessage(ActorsFileSearcher searcher, ActorRef<FileSearchProtocol.FoundFileMessage> fileFinderActor, Path file, String word) {
            this.fileFinderActor=fileFinderActor;
            this.word=word;
            this.file = file;
            this.searcher=searcher;
        }
    }
}

class FileSearchProtocolBehaviour extends AbstractBehavior<FileSearchProtocol.FoundFileMessage> {

    private FileSearchProtocolBehaviour(ActorContext<FileSearchProtocol.FoundFileMessage> context) {
        super(context);
    }

    @Override
    public Receive<FileSearchProtocol.FoundFileMessage> createReceive() {
        return newReceiveBuilder().onMessage(FileSearchProtocol.FoundFileMessage.class, this::onFileFound).build();
    }

    private Behavior<FileSearchProtocol.FoundFileMessage> onFileFound(FileSearchProtocol.FoundFileMessage msg) throws IOException {
        getContext().getLog().info("Found file " + msg.file + " from " + this.getContext().getSelf());

        var isPositive = AFilePDFSearcher.searchWordInPDF(msg.file, msg.word);
        if(isPositive) {
            getContext().getLog().info("File " + msg.file + " from " + this.getContext().getSelf() + " contains text!");

            msg.isPositive = isPositive;
            msg.fileFinderActor.tell(msg);
        }

        msg.searcher.nComputedFiles++;
        try {
            msg.searcher.finishSem.acquire();
            msg.searcher.notifyIfFinished();
            msg.searcher.finishSem.release();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public static Behavior<FileSearchProtocol.FoundFileMessage> create() {
        return Behaviors.setup(FileSearchProtocolBehaviour::new);
    }
}

class PositiveFileSearchProtocolBehaviour extends AbstractBehavior<FileSearchProtocol.FoundFileMessage>{
    public PositiveFileSearchProtocolBehaviour(ActorContext<FileSearchProtocol.FoundFileMessage> context) {
        super(context);
    }

    private Behavior<FileSearchProtocol.FoundFileMessage> onPositiveFound(FileSearchProtocol.FoundFileMessage msg) {
        getContext().getLog().info("File " + msg.file + " from " + this.getContext().getSelf() + " contains text!");

        msg.searcher.AddResultAndNotify(msg.file);
        try {
            msg.searcher.finishSem.acquire();
            msg.searcher.notifyIfFinished();
            msg.searcher.finishSem.release();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    @Override
    public Receive<FileSearchProtocol.FoundFileMessage> createReceive() {
        return newReceiveBuilder()
                .onMessage(FileSearchProtocol.FoundFileMessage.class, this::onPositiveFound)
                .build();
    }

    public static Behavior<FileSearchProtocol.FoundFileMessage> create() {
        return Behaviors.setup(PositiveFileSearchProtocolBehaviour::new);
    }
}