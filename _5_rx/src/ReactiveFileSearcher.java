import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.Semaphore;

public class ReactiveFileSearcher extends AFilePDFSearcher {
    private int nComputedFiles=0;
    private PublishSubject<Path> sourcePaths;
    private Disposable toDispose;
    private Semaphore finishSem = new Semaphore(1);
    private boolean finishAlreadyNotified = false;

    public ReactiveFileSearcher(Path start, String word) {
        super(start, word);
    }

    @Override
    public void start() {
        nComputedFiles=0;

        if(sourcePaths == null) {
            sourcePaths = PublishSubject.<Path>create();

            toDispose = sourcePaths
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.single()) // to decouple search file to computation
                .subscribe(file -> {
                    var isPositive = AFilePDFSearcher.searchWordInPDF(file, word);
                    if(isPositive) {
                        AddResultAndNotify(file);
                        try {
                            finishSem.acquire();
                            notifyIfFinished();
                            finishSem.release();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
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
        sourcePaths.onNext(file);
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
        toDispose.dispose();
    }
}
