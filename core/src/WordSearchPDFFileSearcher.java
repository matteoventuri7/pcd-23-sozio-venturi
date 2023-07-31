import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public abstract class WordSearchPDFFileSearcher extends AFilePDFSearcher {

    private String _searchWord;
    private int _occurrences;

    public WordSearchPDFFileSearcher(Path start, String word) {
        super(start, word);
        _searchWord = word.toLowerCase();
        _occurrences = 0;
    }

    @Override
    protected void onFoundPDFFile(Path file, BasicFileAttributes attrs) {
        try (PDDocument document = PDDocument.load(file.toFile())) {
            PDFTextStripper textStripper = new PDFTextStripper();
            String content = textStripper.getText(document).toLowerCase();
            int count = countOccurrences(content, _searchWord);
            _occurrences += count;
            if (count > 0) {
                System.out.println("Found " + count + " occurrences of the word '" + _searchWord + "' in the file: " + file);
            }
        } catch (IOException e) {
            System.err.println("Error while reading the PDF file: " + file);
        }
    }

    private int countOccurrences(String content, String word) {
        int lastIndex = 0;
        int count = 0;
        while (lastIndex != -1) {
            lastIndex = content.indexOf(word, lastIndex);
            if (lastIndex != -1) {
                count++;
                lastIndex += word.length();
            }
        }
        return count;
    }

    public int getTotalOccurrences() {
        return _occurrences;
    }
}
