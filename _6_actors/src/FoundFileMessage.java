import akka.actor.typed.ActorRef;

import java.nio.file.Path;

public class FoundFileMessage extends BaseSearchMessage{
    // sender
    public final ActorRef<BaseSearchMessage> fileFinderActor;
    public final Path file;
    public final String word;
    // destination
    public boolean isPositive;

    public FoundFileMessage(ActorsFileSearcher searcher, ActorRef<BaseSearchMessage> fileFinderActor, Path file, String word) {
        super(searcher);
        this.fileFinderActor=fileFinderActor;
        this.word=word;
        this.file = file;
    }
}