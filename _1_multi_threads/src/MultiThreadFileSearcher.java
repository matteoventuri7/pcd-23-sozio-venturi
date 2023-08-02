import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

public class MultiThreadFileSearcher extends AFilePDFSearcher {
    protected ExecutorService _threadPool;
    private long _elapsedTime;
    private boolean _fileSearchFinished;
    private volatile boolean _wordFound; // New variable to track if the word is found
    private String _word; // Store the searched word

    public MultiThreadFileSearcher(Path start, String word) {
        super(start, word);
        _word = word;
    }

    @Override
    public void start() {
        if (_threadPool == null || _threadPool.isShutdown()) {
            _threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        }
        super.start();
    }

    @Override
    protected void onFoundPDFFile(Path file, BasicFileAttributes attrs) throws RejectedExecutionException {
        _threadPool.execute(() -> {
            if (_pause) {
                EnqueueFile(file, attrs);
            } else if (!_stop && !_wordFound) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                // Search the word in the PDF
                boolean wordFoundInPDF = false;
                try {
                    wordFoundInPDF = searchWordInPDF(file);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // If word found in PDF, stop the search and increment the count
                if (wordFoundInPDF) {
                    _wordFound = true;
                    _searchResult.addResult(file);
                    _elapsedTime = super.getElapsedTime();
                }
            }
        });
    }

    @Override
    public void close() throws Exception {
        super.close();
        _threadPool.shutdown();
    }

    @Override
    public long getElapsedTime() {
        return _elapsedTime;
    }

    private boolean searchWordInPDF(Path file) throws IOException {
        // Implement your code to search the word in the PDF here
        // You can use libraries like Apache PDFBox to extract text from the PDF and search the word.
        // Return true if word is found, false otherwise.
        // Sample code:

        PDDocument document = PDDocument.load(file.toFile());
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(document);
        document.close();
        return text.contains(_word);

    }
}
