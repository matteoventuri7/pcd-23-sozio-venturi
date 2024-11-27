import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.Semaphore;

public class ReactiveFileSearcher extends AFilePDFSearcher {
    private PublishSubject<Path> sourcePaths;
    private Disposable toDispose;

    public ReactiveFileSearcher(Path start, String word) {
        super(start, word);
    }

    @Override
    public void start() {
        if(sourcePaths == null) {
            sourcePaths = PublishSubject.<Path>create();

            toDispose = sourcePaths
                .subscribeOn(Schedulers.computation())
                .subscribe(file -> {
                    var isPositive = AFilePDFSearcher.searchWordInPDF(file, word);
                    if(isPositive) {
                        searcherLock.lock();
                        addResultAndNotify(file);
                        searcherLock.unlock();
                    }
                }, Throwable::printStackTrace, this::notifyFinish);
        }

        super.start();
    }

    @Override
    protected void onFoundPDFFile(Path file, BasicFileAttributes attrs) throws InterruptedException {
        CheckStartSearch();
        sourcePaths.onNext(file);
    }

    @Override
    protected void onSearchIsFinished() throws InterruptedException {
        super.onSearchIsFinished();
        sourcePaths.onComplete();
    }

    @Override
    public void close() throws Exception {
        super.close();
        toDispose.dispose();
    }
}
