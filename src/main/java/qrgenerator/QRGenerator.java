package qrgenerator;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class QRGenerator extends JFrame {

    private static final long serialVersionUID = 1L;

    private JTextField titleField;
    private JTextField descriptionField;
    private JTextArea dataArea;
    private JTextField fileNameField;

    // Always save in this folder
    private static final String BASE_FOLDER = "folder_qrgenerator";

    public QRGenerator() {
        setTitle("QR Code Generator");
        setSize(520, 420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();

        // Load window icon from resources
        try (InputStream iconStream =
                     getClass().getResourceAsStream("/qrgenerator/pc1.png")) {

            if (iconStream != null) {
                setIconImage(ImageIO.read(iconStream));
            } else {
                System.err.println("Icon not found in resources");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initUI() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel fieldsPanel = new JPanel(new GridLayout(3, 2, 5, 5));

        fieldsPanel.add(new JLabel("Title:"));
        titleField = new JTextField();
        fieldsPanel.add(titleField);

        fieldsPanel.add(new JLabel("Description:"));
        descriptionField = new JTextField();
        fieldsPanel.add(descriptionField);

        fieldsPanel.add(new JLabel("File name:"));
        // Default name: untitled-qr.png inside BASE_FOLDER
        fileNameField = new JTextField("untitled-qr");
        fieldsPanel.add(fileNameField);

        panel.add(fieldsPanel, BorderLayout.NORTH);

        dataArea = new JTextArea(6, 40);
        dataArea.setLineWrap(true);
        dataArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(dataArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("QR Data"));

        panel.add(scrollPane, BorderLayout.CENTER);

        JButton generateButton = new JButton("Generate QR Code");
        generateButton.addActionListener(e -> generateQR());

        panel.add(generateButton, BorderLayout.SOUTH);

        setContentPane(panel);
    }

    private void generateQR() {
        try {
            String title = titleField.getText().trim();
            String description = descriptionField.getText().trim();
            String data = dataArea.getText().trim();
            String baseName = fileNameField.getText().trim();

            if (data.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "QR data cannot be empty",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            if (baseName.isEmpty()) {
                baseName = "untitled-qr";
            }

            String fullFileName = BASE_FOLDER + "/" + baseName;
            if (!fullFileName.toLowerCase().endsWith(".png")) {
                fullFileName += ".png";
            }

            createQRImage(title, description, data, fullFileName);

            JOptionPane.showMessageDialog(
                    this,
                    "QR code saved as:\n" + new File(fullFileName).getAbsolutePath()
            );

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to generate QR",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void createQRImage(
            String title,
            String description,
            String data,
            String fileName
    ) throws Exception {

        int qrSize = 600;
        int width = 600;
        int height = 720;

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix =
                qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, qrSize, qrSize);

        BufferedImage qrImage =
                MatrixToImageWriter.toBufferedImage(bitMatrix);

        BufferedImage finalImage =
                new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Graphics2D g = finalImage.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.setColor(Color.BLACK);

        int y = 30;

        if (!title.isEmpty()) {
            g.setFont(new Font("SansSerif", Font.BOLD, 22));
            y = drawCentered(g, title, width, y);
        }

        int qrX = (width - qrSize) / 2;
        g.drawImage(qrImage, qrX, y + 10, null);
        y += qrSize + 30;

        if (!description.isEmpty()) {
            g.setFont(new Font("SansSerif", Font.PLAIN, 16));
            drawCentered(g, description, width, y);
        }

        g.dispose();

        // ---------- ENSURE DIRECTORY EXISTS ----------
        File outputFile = new File(fileName);
        File parent = outputFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        ImageIO.write(finalImage, "PNG", outputFile);
    }

    private int drawCentered(Graphics2D g, String text, int width, int y) {
        FontMetrics fm = g.getFontMetrics();
        int x = (width - fm.stringWidth(text)) / 2;
        g.drawString(text, x, y);
        return y + fm.getHeight();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new QRGenerator().setVisible(true)
        );
    }
}
