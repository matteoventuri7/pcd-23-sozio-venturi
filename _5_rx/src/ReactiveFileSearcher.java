import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class ReactiveFileSearcher extends AFilePDFSearcher {
    private int nComputedFiles=0;
    PublishSubject<Path> sourcePaths;
    Disposable toDispose;

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
                .subscribe(file -> {
                    var isPositive = AFilePDFSearcher.searchWordInPDF(file, word);
                    if(isPositive) {
                        AddResultAndNotify(file);
                        notifyIfFinished();
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
        toDispose.dispose();
    }
}
