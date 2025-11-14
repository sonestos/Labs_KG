import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import java.io.File;

public class Main {
    private JFrame frame;
    private JLabel originalImageLabel;
    private JLabel processedImageLabel;
    private JComboBox<String> methodComboBox;
    private JLabel statusLabel;
    private BufferedImage originalImage;
    private BufferedImage processedImage;

    private static final String EDGE_DETECTION = "Обнаружение перепадов яркости (Sobel)";
    private static final String LINE_DETECTION = "Обнаружение линий";
    private static final String POINT_DETECTION = "Обнаружение точек";
    private static final String LOCAL_THRESHOLD_MEAN = "Локальная пороговая обработка Ниблэка";
    private static final String LOCAL_THRESHOLD_GAUSSIAN = "Локальная пороговая обработка Бернсена";
    private static final String COMPRESSION_RLE = "Сжатие RLE";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Main().createAndShowGUI();
        });
    }

    public void createAndShowGUI() {
        frame = new JFrame("Обработка изображений - Сегментация и Сжатие");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        createControlPanel();
        createImagePanel();
        createStatusPanel();

        frame.pack();
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setBorder(BorderFactory.createTitledBorder("Управление"));
        controlPanel.setLayout(new FlowLayout());

        JLabel methodLabel = new JLabel("Метод обработки:");
        methodComboBox = new JComboBox<>(new String[]{
                EDGE_DETECTION,
                LINE_DETECTION,
                POINT_DETECTION,
                LOCAL_THRESHOLD_MEAN,
                LOCAL_THRESHOLD_GAUSSIAN,
                COMPRESSION_RLE
        });

        JButton processButton = new JButton("Обработать");
        JButton loadUrlButton = new JButton("Загрузить по URL");
        JButton loadFileButton = new JButton("Загрузить из файла");

        processButton.addActionListener(e -> processImage());
        loadUrlButton.addActionListener(e -> loadImageFromUrl());
        loadFileButton.addActionListener(e -> loadImageFromFile());

        controlPanel.add(methodLabel);
        controlPanel.add(methodComboBox);
        controlPanel.add(processButton);
        controlPanel.add(loadUrlButton);
        controlPanel.add(loadFileButton);

        frame.add(controlPanel, BorderLayout.NORTH);
    }

    private void createImagePanel() {
        JPanel imagePanel = new JPanel(new GridLayout(1, 2, 10, 10));
        imagePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Оригинальное изображение
        JPanel originalPanel = new JPanel(new BorderLayout());
        originalPanel.setBorder(BorderFactory.createTitledBorder("Оригинальное изображение"));
        originalImageLabel = new JLabel("Нажмите 'Загрузить по URL' или 'Загрузить из файла'", JLabel.CENTER);
        originalImageLabel.setPreferredSize(new Dimension(500, 400));
        originalImageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        originalPanel.add(originalImageLabel, BorderLayout.CENTER);

        JPanel processedPanel = new JPanel(new BorderLayout());
        processedPanel.setBorder(BorderFactory.createTitledBorder("Обработанное изображение"));
        processedImageLabel = new JLabel("Результат обработки", JLabel.CENTER);
        processedImageLabel.setPreferredSize(new Dimension(500, 400));
        processedImageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        processedPanel.add(processedImageLabel, BorderLayout.CENTER);

        imagePanel.add(originalPanel);
        imagePanel.add(processedPanel);

        frame.add(imagePanel, BorderLayout.CENTER);
    }

    private void createStatusPanel() {
        statusLabel = new JLabel(" Загрузите изображение для начала работы");
        statusLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        frame.add(statusLabel, BorderLayout.SOUTH);
    }

    private void loadImageFromUrl() {
        String urlString = JOptionPane.showInputDialog(frame,
                "Введите URL изображения:",
                "Загрузка по URL",
                JOptionPane.QUESTION_MESSAGE);

        if (urlString != null && !urlString.trim().isEmpty()) {
            try {
                statusLabel.setText(" Загрузка изображения...");

                URL url = new URL(urlString);
                originalImage = ImageIO.read(url);

                if (originalImage != null) {
                    displayImage(originalImage, originalImageLabel);
                    statusLabel.setText(" Изображение успешно загружено по URL. Размер: " +
                            originalImage.getWidth() + "x" + originalImage.getHeight());
                } else {
                    JOptionPane.showMessageDialog(frame, "Не удалось загрузить изображение по указанному URL",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText(" Ошибка загрузки изображения");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, "Ошибка загрузки: " + e.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText(" Ошибка загрузки изображения");
            }
        }
    }

    private void loadImageFromFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Выберите изображение");

        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                String name = f.getName().toLowerCase();
                return name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                        name.endsWith(".png") || name.endsWith(".bmp") ||
                        name.endsWith(".gif");
            }

            @Override
            public String getDescription() {
                return "Изображения (*.jpg, *.jpeg, *.png, *.bmp, *.gif)";
            }
        });

        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                statusLabel.setText(" Загрузка изображения...");

                File selectedFile = fileChooser.getSelectedFile();
                originalImage = ImageIO.read(selectedFile);

                if (originalImage != null) {
                    displayImage(originalImage, originalImageLabel);
                    statusLabel.setText(" Изображение успешно загружено из файла. Размер: " +
                            originalImage.getWidth() + "x" + originalImage.getHeight());
                } else {
                    JOptionPane.showMessageDialog(frame, "Не удалось загрузить изображение",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText(" Ошибка загрузки изображения");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, "Ошибка загрузки: " + e.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText(" Ошибка загрузки изображения");
            }
        }
    }

    private void processImage() {
        if (originalImage == null) {
            JOptionPane.showMessageDialog(frame, "Сначала загрузите изображение",
                    "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String method = (String) methodComboBox.getSelectedItem();
        long startTime = System.currentTimeMillis();

        try {
            switch (method) {
                case EDGE_DETECTION:
                    processedImage = applyEdgeDetection(originalImage);
                    break;
                case LINE_DETECTION:
                    processedImage = applyLineDetection(originalImage);
                    break;
                case POINT_DETECTION:
                    processedImage = applyPointDetection(originalImage);
                    break;
                case LOCAL_THRESHOLD_MEAN:
                    processedImage = applyLocalThreshold(originalImage);
                    break;
                case LOCAL_THRESHOLD_GAUSSIAN:
                    processedImage = applyLocalThresholdBernsen(originalImage);
                    break;
                case COMPRESSION_RLE:
                    processedImage = applyBlockCompression(originalImage);
                    break;
            }

            displayImage(processedImage, processedImageLabel);
            long endTime = System.currentTimeMillis();
            statusLabel.setText(String.format(" Обработка завершена (%s). Время: %d мс",
                    method, endTime - startTime));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Ошибка обработки: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private BufferedImage applyEdgeDetection(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        int[][] sobelX = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
        int[][] sobelY = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                double gx = 0, gy = 0;

                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        Color color = new Color(image.getRGB(x + kx, y + ky));
                        double gray = (color.getRed() + color.getGreen() + color.getBlue()) / 3.0;

                        gx += gray * sobelX[ky + 1][kx + 1];
                        gy += gray * sobelY[ky + 1][kx + 1];
                    }
                }

                double magnitude = Math.sqrt(gx * gx + gy * gy);
                int grayValue = (int) Math.min(255, magnitude * 3);
                int rgb = (grayValue << 16) | (grayValue << 8) | grayValue;
                result.setRGB(x, y, rgb);
            }
        }

        return result;
    }

    private BufferedImage applyLineDetection(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        int[][] horizontalKernel = {{-1, -1, -1}, {2, 2, 2}, {-1, -1, -1}};

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                double sum = 0;

                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        Color color = new Color(image.getRGB(x + kx, y + ky));
                        double gray = (color.getRed() + color.getGreen() + color.getBlue()) / 3.0;
                        sum += gray * horizontalKernel[ky + 1][kx + 1];
                    }
                }

                int grayValue = (int) Math.min(255, Math.abs(sum) / 2);
                int rgb = (grayValue << 16) | (grayValue << 8) | grayValue;
                result.setRGB(x, y, rgb);
            }
        }

        return result;
    }

    private BufferedImage applyPointDetection(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        int[][] laplacian = {{-1, -1, -1}, {-1, 8, -1}, {-1, -1, -1}};

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                double sum = 0;

                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        Color color = new Color(image.getRGB(x + kx, y + ky));
                        double gray = (color.getRed() + color.getGreen() + color.getBlue()) / 3.0;
                        sum += gray * laplacian[ky + 1][kx + 1];
                    }
                }

                int grayValue = (int) Math.min(255, Math.abs(sum));
                int rgb = (grayValue << 16) | (grayValue << 8) | grayValue;
                result.setRGB(x, y, rgb);
            }
        }

        return result;
    }

    private BufferedImage applyLocalThreshold(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int windowSize = 15;
        int halfWindow = windowSize / 2;
        double k = -0.2;
        double[][] grayImage = new double[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));
                grayImage[y][x] = (color.getRed() + color.getGreen() + color.getBlue()) / 3.0;
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double sum = 0;
                double sumSquares = 0;
                int count = 0;

                int yStart = Math.max(0, y - halfWindow);
                int yEnd = Math.min(height - 1, y + halfWindow);
                int xStart = Math.max(0, x - halfWindow);
                int xEnd = Math.min(width - 1, x + halfWindow);

                for (int ny = yStart; ny <= yEnd; ny++) {
                    for (int nx = xStart; nx <= xEnd; nx++) {
                        double gray = grayImage[ny][nx];
                        sum += gray;
                        sumSquares += gray * gray;
                        count++;
                    }
                }

                double mean = sum / count;
                double variance = (sumSquares / count) - (mean * mean);
                double stdDev = Math.sqrt(variance);

                double threshold = mean + k * stdDev;
                double originalGray = grayImage[y][x];

                int grayValue = (originalGray > threshold) ? 255 : 0;
                int rgb = (grayValue << 16) | (grayValue << 8) | grayValue;
                result.setRGB(x, y, rgb);
            }
        }
        return result;
    }

    private BufferedImage applyLocalThresholdBernsen(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int windowSize = 15;
        int halfWindow = windowSize / 2;
        int contrastThreshold = 15;

        double[][] grayImage = new double[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));
                grayImage[y][x] = (color.getRed() + color.getGreen() + color.getBlue()) / 3.0;
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double min = 255;
                double max = 0;

                int yStart = Math.max(0, y - halfWindow);
                int yEnd = Math.min(height - 1, y + halfWindow);
                int xStart = Math.max(0, x - halfWindow);
                int xEnd = Math.min(width - 1, x + halfWindow);

                for (int ny = yStart; ny <= yEnd; ny++) {
                    for (int nx = xStart; nx <= xEnd; nx++) {
                        double gray = grayImage[ny][nx];
                        if (gray < min) min = gray;
                        if (gray > max) max = gray;
                    }
                }

                double contrast = max - min;
                double threshold = (min + max) / 2;
                double originalGray = grayImage[y][x];

                int grayValue;
                if (contrast < contrastThreshold) {
                    grayValue = (originalGray > 128) ? 255 : 0;
                } else {
                    grayValue = (originalGray > threshold) ? 255 : 0;
                }

                int rgb = (grayValue << 16) | (grayValue << 8) | grayValue;
                result.setRGB(x, y, rgb);
            }
        }
        return result;
    }

    private BufferedImage applyBlockCompression(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int blockSize = 8;

        BufferedImage compressed = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int by = 0; by < height; by += blockSize) {
            for (int bx = 0; bx < width; bx += blockSize) {
                int sumR = 0, sumG = 0, sumB = 0;
                int count = 0;

                for (int y = by; y < by + blockSize && y < height; y++) {
                    for (int x = bx; x < bx + blockSize && x < width; x++) {
                        Color c = new Color(image.getRGB(x, y));
                        sumR += c.getRed();
                        sumG += c.getGreen();
                        sumB += c.getBlue();
                        count++;
                    }
                }

                int avgR = sumR / count;
                int avgG = sumG / count;
                int avgB = sumB / count;
                Color avgColor = new Color(avgR, avgG, avgB);

                for (int y = by; y < by + blockSize && y < height; y++) {
                    for (int x = bx; x < bx + blockSize && x < width; x++) {
                        compressed.setRGB(x, y, avgColor.getRGB());
                    }
                }
            }
        }

        return compressed;
    }

    private void displayImage(BufferedImage image, JLabel label) {
        if (image != null) {
            Image scaledImage = image.getScaledInstance(500, 400, Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(scaledImage));
            label.setText("");
        }
    }
}