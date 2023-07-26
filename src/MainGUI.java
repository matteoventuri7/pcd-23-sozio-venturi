import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;

public class MainGUI {
    private static boolean isClosing = false;
    private static IWordSearcher s;
    private static Thread updateOutputAreaThread;
    private static JTextArea outputArea;
    private static JLabel totalFilesLabel;
    private static JLabel foundPdfFilesLabel;
    private static JLabel pdfFilesWithKeywordLabel;
    private static JButton stopButton;
    private static JButton startButton;
    private static String keyword; // Variable to store the searched keyword
    private static volatile boolean isStopping = false;
    private static volatile boolean isSuspended = false;


    public static void main(String[] args) {
        JFrame frame = new JFrame("File Search GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                isClosing = true;

                if (s != null) {
                    try {
                        s.stop();
                        s.close();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                if (updateOutputAreaThread != null) {
                    try {
                        updateOutputAreaThread.join(Duration.ofSeconds(5));
                    } catch (InterruptedException e) {
                        // ops
                    }
                }
            }
        });
        frame.setSize(800, 800);
        frame.setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        JTextField folderField = new JTextField("/Users/diegosozio/Documents/PCD/test", 20);
        JTextField keywordField = new JTextField("ciao", 20);
        startButton = new JButton("Start");
        stopButton = new JButton("Stop");
        JButton suspendButton = new JButton("Suspend");
        JButton resumeButton = new JButton("Resume");

        totalFilesLabel = new JLabel("Total files: 0");
        foundPdfFilesLabel = new JLabel("Found PDF files: 0");
        pdfFilesWithKeywordLabel = new JLabel("PDF files with keyword: 0");

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        inputPanel.add(new JLabel("Folder:"));
        inputPanel.add(folderField);
        inputPanel.add(new JLabel("Keyword:"));
        inputPanel.add(keywordField);
        inputPanel.add(startButton);
        inputPanel.add(stopButton);
        inputPanel.add(suspendButton);
        inputPanel.add(resumeButton);
        inputPanel.add(totalFilesLabel);
        inputPanel.add(foundPdfFilesLabel);
        inputPanel.add(pdfFilesWithKeywordLabel);

        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String folderPath = folderField.getText();
                keyword = keywordField.getText().trim(); // Save the searched keyword
                System.out.println("STARTED");
                stopButton.setEnabled(true);

                // Clear the output area before starting a new search
                outputArea.setText("");
                totalFilesLabel.setText("Total files: 0");
                foundPdfFilesLabel.setText("Found PDF files: 0");
                pdfFilesWithKeywordLabel.setText("PDF files with keyword: 0");

                try {
                    s = new MultiThreadFileSearcher(Path.of(folderPath), keyword);
                    s.search();
                } catch (Exception ex) {
                    outputArea.append("Error: " + ex.getMessage() + "\n");
                }
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (s != null) {
                    try {
                        s.stop();
                        isStopping = true; // Imposta il flag per fermare il thread
                        System.out.println("STOPPED");
                        stopButton.setEnabled(false);
                        startButton.setEnabled(true); // Abilita il pulsante "Start"
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });


        suspendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (s != null) {
                    s.pause();
                    isSuspended = true; // Imposta il flag per indicare che la ricerca è sospesa
                    stopButton.setEnabled(false);
                    suspendButton.setEnabled(false);
                    resumeButton.setEnabled(true);
                }
            }
        });

        resumeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (s != null) {
                    s.pause();
                    isSuspended = false; // Ripristina il flag per indicare che la ricerca non è più sospesa
                    stopButton.setEnabled(true);
                    suspendButton.setEnabled(true);
                    resumeButton.setEnabled(false);

                    // Rimuovi il testo dell'area di output quando si preme "Resume"
                    outputArea.setText("");
                    totalFilesLabel.setText("Total files: 0");
                    foundPdfFilesLabel.setText("Found PDF files: 0");
                    pdfFilesWithKeywordLabel.setText("PDF files with keyword: 0");
                    keyword = keywordField.getText().trim(); // Aggiorna la parola chiave cercata
                }
            }
        });


        frame.setVisible(true);
        updateOutputArea();
    }

    private static void updateOutputArea() {
        updateOutputAreaThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (!isSuspended && s != null && s.getResult() != null) {
                        var result = s.getResult();
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                outputArea.setText("");
                                outputArea.append("IsPartial: " + result.isPartial() + "\n");
                                totalFilesLabel.setText("Total files: " + result.getTotalFiles());
                                foundPdfFilesLabel.setText("Found PDF files: " + result.getFiles().size());

                                // Reset the counter for PDF files with the keyword
                                int pdfWithKeyword = 0;
                                for (Path file : result.getFiles()) {
                                    outputArea.append(file.toString() + "\n");
                                    if (file.toString().toLowerCase().endsWith(".pdf")) {
                                        // Check if the filename contains the keyword
                                        if (file.getFileName().toString().toLowerCase().contains(keyword)) {
                                            pdfWithKeyword++;
                                        }
                                    }
                                }
                                pdfFilesWithKeywordLabel.setText("PDF files with keyword: " + pdfWithKeyword);
                            }
                        });
                    }

                    try {
                        Thread.sleep(100); // Aggiungi un breve ritardo per evitare busy-waiting
                    } catch (InterruptedException e) {
                        // Va bene
                    }

                    if (isClosing) {
                        break;
                    }

                    // Check if the search is stopped, then wait until it's resumed
                    while (isStopping) {
                        try {
                            Thread.sleep(100); // Add a short delay to avoid busy-waiting
                        } catch (InterruptedException e) {
                            // Va bene
                        }
                    }
                }
            }
        });
        updateOutputAreaThread.setDaemon(true);
        updateOutputAreaThread.start();
    }
}

