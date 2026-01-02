package screencapture;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import javax.imageio.ImageIO;

public class IntervalScreenCapture {

    private static final String BASE_DIR = "folder_screencapture";
    private static final String CONFIG_PATH = BASE_DIR + "/config.file";

    private static void createDefaultConfigIfMissing(File configFile) {
        if (configFile.exists()) {
            return;
        }

        // Ensure parent directory exists
        File parent = configFile.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(configFile))) {
            writer.println("Screenshot per minute:");
            writer.println("3");
            writer.println();
            writer.println("Folder name:");
            writer.println("folder1");
        } catch (IOException e) {
            throw new RuntimeException("Failed to create default config.file", e);
        }
    }

    private static void takeScreenShot(File directory) {
        try {
            Robot robot = new Robot();

            Rectangle screenRect =
                    new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

            BufferedImage screenFullImage =
                    robot.createScreenCapture(screenRect);

            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

            String fileName =
                    LocalDateTime.now().format(formatter) + ".png";

            File outputFile = new File(directory, fileName);

            ImageIO.write(screenFullImage, "PNG", outputFile);

        } catch (AWTException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws InterruptedException {

        File configFile = new File(CONFIG_PATH);

        // CREATE DEFAULT CONFIG IF MISSING
        createDefaultConfigIfMissing(configFile);

        int frequency = 60000; // base: 1 minute
        String folder;

        // READ CONFIG
        try (Scanner scan = new Scanner(new FileReader(configFile))) {

            scan.nextLine(); // "Screenshot per minute:"
            frequency /= Integer.parseInt(scan.nextLine());

            scan.nextLine(); // blank
            scan.nextLine(); // "Folder name:"
            folder = scan.nextLine();

        } catch (Exception e) {
            throw new RuntimeException("Failed to read config.file", e);
        }

        // DIRECTORY SETUP
        File baseStorage = new File(BASE_DIR + "/storage");
        File toolFolder = new File(baseStorage, folder);
        File dateFolder = new File(toolFolder, LocalDate.now().toString());

        if (!dateFolder.exists()) {
            boolean created = dateFolder.mkdirs();
            if (!created) {
                throw new RuntimeException(
                        "Failed to create directory: " +
                        dateFolder.getAbsolutePath()
                );
            }
        }

        // MAIN LOOP
        while (true) {
            takeScreenShot(dateFolder);
            Thread.sleep(frequency);
        }
    }
}
