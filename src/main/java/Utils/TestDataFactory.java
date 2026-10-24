package Utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.concurrent.ThreadLocalRandom;

import javax.imageio.ImageIO;

/**
 * Test data helpers.
 *
 * <p>The server rejects a second <b>pending</b> application for the same mobile
 * number, so every test that submits an application must use {@link #uniqueMobile()}.</p>
 */
public final class TestDataFactory {

    public static final String VALID_NAME = "Priya Sharma";
    public static final String VALID_ADDRESS = "Flat B-204, Green Meadows Society, Sector 12, Dwarka, New Delhi 110075";
    public static final String VALID_FAMILY = "4";
    public static final String VALID_ID_TYPE = "Other";
    public static final String VALID_GENDER = "Female";
    public static final String VALID_DOC_REF = "DEMO-1234";

    private static final File FILE_DIR =
            new File(System.getProperty("user.dir"), "target" + File.separator + "test-files");

    private TestDataFactory() {
    }

    /** A random valid Indian mobile number (10 digits, starts with 9). */
    public static String uniqueMobile() {
        long n = ThreadLocalRandom.current().nextLong(100_000_000L, 1_000_000_000L);
        return "9" + n;
    }

    /** A string of the requested length made of a repeated character. */
    public static String repeat(char c, int length) {
        return String.valueOf(c).repeat(length);
    }

    /** A valid PNG image of the given size. */
    public static File pngImage(int width, int height) throws IOException {
        File file = new File(ensureDir(), "photo_" + width + "x" + height + ".png");
        if (!file.exists()) {
            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            g.setColor(new Color(30, 91, 63));
            g.fillRect(0, 0, width, height);
            g.setColor(Color.WHITE);
            g.fillOval(width / 4, height / 4, width / 2, height / 2);
            g.dispose();
            ImageIO.write(img, "png", file);
        }
        return file;
    }

    /** A valid JPG image. */
    public static File jpgImage() throws IOException {
        File file = new File(ensureDir(), "photo.jpg");
        if (!file.exists()) {
            BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            g.setColor(new Color(159, 211, 178));
            g.fillRect(0, 0, 200, 200);
            g.dispose();
            ImageIO.write(img, "jpg", file);
        }
        return file;
    }

    /** A plain-text file (wrong type for the photo field). */
    public static File textFile() throws IOException {
        File file = new File(ensureDir(), "not_an_image.txt");
        if (!file.exists()) {
            Files.writeString(file.toPath(), "This is not an image.");
        }
        return file;
    }

    /** A .png file larger than the 5 MB limit. */
    public static File oversizedPng() throws IOException {
        File file = new File(ensureDir(), "oversized_6mb.png");
        if (!file.exists()) {
            try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
                raf.setLength(6L * 1024 * 1024);
            }
        }
        return file;
    }

    /** A file with a .png extension whose content is not an image. */
    public static File corruptPng() throws IOException {
        File file = new File(ensureDir(), "corrupt.png");
        if (!file.exists()) {
            Files.writeString(file.toPath(), "definitely not png bytes");
        }
        return file;
    }

    private static File ensureDir() {
        FILE_DIR.mkdirs();
        return FILE_DIR;
    }
}
