import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class Main extends JFrame {

    private boolean updating = false;
    private boolean fromCmyk = false;
    private boolean fromHsv = false;

    private final JSlider rSlider = new JSlider(0, 255, 0);
    private final JSlider gSlider = new JSlider(0, 255, 0);
    private final JSlider bSlider = new JSlider(0, 255, 0);
    private final JTextField rField = new JTextField("0", 4);
    private final JTextField gField = new JTextField("0", 4);
    private final JTextField bField = new JTextField("0", 4);

    private final JSlider cSlider = new JSlider(0, 100, 0);
    private final JSlider mSlider = new JSlider(0, 100, 0);
    private final JSlider ySlider = new JSlider(0, 100, 0);
    private final JSlider kSlider = new JSlider(0, 100, 0);
    private final JTextField cField = new JTextField("0", 4);
    private final JTextField mField = new JTextField("0", 4);
    private final JTextField yField = new JTextField("0", 4);
    private final JTextField kField = new JTextField("0", 4);

    private final JSlider hSlider = new JSlider(0, 360, 0);
    private final JSlider sSlider = new JSlider(0, 100, 0);
    private final JSlider vSlider = new JSlider(0, 100, 0);
    private final JTextField hField = new JTextField("0", 4);
    private final JTextField sField = new JTextField("0", 4);
    private final JTextField vField = new JTextField("0", 4);

    private final JPanel colorPreview = new JPanel();
   // private final JButton colorChooserBtn = new JButton("Выбрать цвет");
    private final JTextField hexField = new JTextField("#000000", 8);

    private static final float PRECISION_EPSILON = 1e-6f;

    public Main() {
        super();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 550);
        setLayout(new BorderLayout(10, 10));

        JPanel modelsPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        modelsPanel.add(createColorPanel("RGB", new String[]{"R", "G", "B"},
                new JSlider[]{rSlider, gSlider, bSlider},
                new JTextField[]{rField, gField, bField}));
        modelsPanel.add(createColorPanel("CMYK (%)", new String[]{"C", "M", "Y", "K"},
                new JSlider[]{cSlider, mSlider, ySlider, kSlider},
                new JTextField[]{cField, mField, yField, kField}));
        modelsPanel.add(createColorPanel("HSV", new String[]{"H", "S", "V"},
                new JSlider[]{hSlider, sSlider, vSlider},
                new JTextField[]{hField, sField, vField}));

        add(modelsPanel, BorderLayout.CENTER);
        add(createRightPanel(), BorderLayout.EAST);

        addFieldListener(rField, rSlider, 0, 255);
        addFieldListener(gField, gSlider, 0, 255);
        addFieldListener(bField, bSlider, 0, 255);

        addCmykFieldListener(cField, cSlider, 0, 100);
        addCmykFieldListener(mField, mSlider, 0, 100);
        addCmykFieldListener(yField, ySlider, 0, 100);
        addCmykFieldListener(kField, kSlider, 0, 100);

        addHsvFieldListener(hField, hSlider, 0, 360);
        addHsvFieldListener(sField, sSlider, 0, 100);
        addHsvFieldListener(vField, vSlider, 0, 100);

        setupListeners();
        updateFromRGB(255, 255, 255);

        setVisible(true);
    }

    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));

        colorPreview.setBackground(Color.BLACK);
        colorPreview.setPreferredSize(new Dimension(220, 220));
        colorPreview.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        rightPanel.add(colorPreview, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new GridLayout(4, 1, 5, 5));

        JPanel hexPanel = new JPanel();
        hexPanel.add(new JLabel("HEX:"));
        hexPanel.add(hexField);

        JPanel palettePanel = new JPanel(new GridLayout(2, 5, 5, 5));
        Color[] baseColors = {
                Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN,
                Color.MAGENTA, Color.ORANGE, Color.PINK, Color.WHITE, Color.BLACK
        };
        for (Color c : baseColors) {
            JButton btn = new JButton();
            btn.setBackground(c);
            btn.setPreferredSize(new Dimension(20, 20));
            btn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            btn.addActionListener(e -> {
                updating = true;
                updateFromRGB(c.getRed(), c.getGreen(), c.getBlue());
                updating = false;
            });
            palettePanel.add(btn);
        }

        controlPanel.add(hexPanel);
        controlPanel.add(palettePanel);
      //  controlPanel.add(colorChooserBtn);

        rightPanel.add(controlPanel, BorderLayout.SOUTH);
        return rightPanel;
    }

    private JPanel createColorPanel(String title, String[] labels, JSlider[] sliders, JTextField[] fields) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.gridx = 0; gbc.gridy = 0;

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            panel.add(new JLabel(labels[i] + ":"), gbc);
            gbc.gridx = 1;
            sliders[i].setPaintTicks(true);
            panel.add(sliders[i], gbc);
            gbc.gridx = 2;
            fields[i].setHorizontalAlignment(JTextField.CENTER);
            panel.add(fields[i], gbc);
            gbc.gridy++;
        }
        return panel;
    }

    private void setupListeners() {

        ChangeListener rgbListener = e -> {
            if (updating) return;
            updating = true;
            updateFromRGB(rSlider.getValue(), gSlider.getValue(), bSlider.getValue());
            updating = false;
        };
        rSlider.addChangeListener(rgbListener);
        gSlider.addChangeListener(rgbListener);
        bSlider.addChangeListener(rgbListener);

        ChangeListener cmykListener = e -> {
            if (updating) return;
            updating = true;
            updateFromCMYK();
            updating = false;
        };
        cSlider.addChangeListener(cmykListener);
        mSlider.addChangeListener(cmykListener);
        ySlider.addChangeListener(cmykListener);
        kSlider.addChangeListener(cmykListener);

        ChangeListener hsvListener = e -> {
            if (updating) return;
            updating = true;
            updateFromHSV();
            updating = false;
        };
        hSlider.addChangeListener(hsvListener);
        sSlider.addChangeListener(hsvListener);
        vSlider.addChangeListener(hsvListener);

        hexField.addActionListener(e -> {
            String text = hexField.getText().trim();
            if (text.startsWith("#")) text = text.substring(1);
            try {
                Color color = new Color(Integer.parseInt(text, 16));
                updating = true;
                updateFromRGB(color.getRed(), color.getGreen(), color.getBlue());
                updating = false;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Неверный HEX-формат!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        /*встроенная в Java палитра JColorChooser
         не лучший вариант для проверки правильности,
         тк при нажатии на эту кнопку срабатывает пересчет из RGB,
         а если мы меняем что-то в ней, например, HSV,
         то оно снова пересчитывается в RGB и обратно,
         из-за лишних оперваций получаются слишком большие погрешности,
         поэтому исправленный код проверялся посредством сравнивания
         результатов с конвертатором цветов онлайн

         colorChooserBtn.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(this, "Выбор цвета", colorPreview.getBackground());
            if (chosen != null) {
                updating = true;
                updateFromRGB(chosen.getRed(), chosen.getGreen(), chosen.getBlue());
                updating = false;
            }
        });*/
    }

    private void updateFromRGB(int r, int g, int b) {
        colorPreview.setBackground(new Color(r, g, b));
        hexField.setText(String.format("#%02X%02X%02X", r, g, b));

        rSlider.setValue(r);
        gSlider.setValue(g);
        bSlider.setValue(b);
        rField.setText(String.valueOf(r));
        gField.setText(String.valueOf(g));
        bField.setText(String.valueOf(b));

        if (!fromCmyk) {
            float[] cmyk = rgbToCmykExact(r, g, b);
            cSlider.setValue((int) Math.round(cmyk[0] * 100));
            mSlider.setValue((int) Math.round(cmyk[1] * 100));
            ySlider.setValue((int) Math.round(cmyk[2] * 100));
            kSlider.setValue((int) Math.round(cmyk[3] * 100));
            cField.setText(String.valueOf(cSlider.getValue()));
            mField.setText(String.valueOf(mSlider.getValue()));
            yField.setText(String.valueOf(ySlider.getValue()));
            kField.setText(String.valueOf(kSlider.getValue()));
        }

        if (!fromHsv) {
            float[] hsv = rgbToHsvExact(r, g, b);
            hSlider.setValue((int) Math.round(hsv[0]));
            sSlider.setValue((int) Math.round(hsv[1] * 100));
            vSlider.setValue((int) Math.round(hsv[2] * 100));
            hField.setText(String.valueOf(hSlider.getValue()));
            sField.setText(String.valueOf(sSlider.getValue()));
            vField.setText(String.valueOf(vSlider.getValue()));
        }
    }

    private void updateFromCMYK() {
        fromCmyk = true;

        float c = cSlider.getValue() / 100.0f;
        float m = mSlider.getValue() / 100.0f;
        float y = ySlider.getValue() / 100.0f;
        float k = kSlider.getValue() / 100.0f;
        int[] rgb = cmykToRgbExact(c, m, y, k);

        cField.setText(String.valueOf(cSlider.getValue()));
        mField.setText(String.valueOf(mSlider.getValue()));
        yField.setText(String.valueOf(ySlider.getValue()));
        kField.setText(String.valueOf(kSlider.getValue()));

        updateFromRGB(rgb[0], rgb[1], rgb[2]);

        fromCmyk = false;
    }

    private void updateFromHSV() {
        fromHsv = true;

        float h = hSlider.getValue();
        float s = sSlider.getValue() / 100.0f;
        float v = vSlider.getValue() / 100.0f;
        int[] rgb = hsvToRgbExact(h, s, v);

        hField.setText(String.valueOf(hSlider.getValue()));
        sField.setText(String.valueOf(sSlider.getValue()));
        vField.setText(String.valueOf(vSlider.getValue()));

        updateFromRGB(rgb[0], rgb[1], rgb[2]);

        fromHsv = false;
    }


    private static float[] rgbToCmykExact(int R, int G, int B) {
        if (R == 0 && G == 0 && B == 0) {
            return new float[]{0.0f, 0.0f, 0.0f, 1.0f};
        }

        float r = R / 255.0f;
        float g = G / 255.0f;
        float b = B / 255.0f;

        float k = 1.0f - Math.max(r, Math.max(g, b));
        float invK = 1.0f - k;

        if (invK < PRECISION_EPSILON) {
            return new float[]{0.0f, 0.0f, 0.0f, 1.0f};
        }

        float c = (1.0f - r - k) / invK;
        float m = (1.0f - g - k) / invK;
        float y = (1.0f - b - k) / invK;

        c = Math.max(0.0f, Math.min(1.0f, c));
        m = Math.max(0.0f, Math.min(1.0f, m));
        y = Math.max(0.0f, Math.min(1.0f, y));
        k = Math.max(0.0f, Math.min(1.0f, k));

        return new float[]{c, m, y, k};
    }

    private static int[] cmykToRgbExact(float c, float m, float y, float k) {
        float invK = 1.0f - k;

        int r = (int) Math.round(255.0f * (1.0f - c) * invK);
        int g = (int) Math.round(255.0f * (1.0f - m) * invK);
        int b = (int) Math.round(255.0f * (1.0f - y) * invK);

        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));

        return new int[]{r, g, b};
    }

    private static float[] rgbToHsvExact(int R, int G, int B) {
        float r = R / 255.0f;
        float g = G / 255.0f;
        float b = B / 255.0f;

        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;

        float h = 0.0f;
        float s = (max > PRECISION_EPSILON) ? (delta / max) : 0.0f;
        float v = max;

        if (delta > PRECISION_EPSILON) {
            if (Math.abs(max - r) < PRECISION_EPSILON) {
                h = 60.0f * (((g - b) / delta) % 6.0f);
            } else if (Math.abs(max - g) < PRECISION_EPSILON) {
                h = 60.0f * (((b - r) / delta) + 2.0f);
            } else {
                h = 60.0f * (((r - g) / delta) + 4.0f);
            }

            if (h < 0.0f) {
                h += 360.0f;
            }
        }

        h = (h % 360.0f + 360.0f) % 360.0f;
        s = Math.max(0.0f, Math.min(1.0f, s));
        v = Math.max(0.0f, Math.min(1.0f, v));

        return new float[]{h, s, v};
    }

    private static int[] hsvToRgbExact(float h, float s, float v) {

        float hNormalized = (h % 360.0f + 360.0f) % 360.0f;
        float sClamped = Math.max(0.0f, Math.min(1.0f, s));
        float vClamped = Math.max(0.0f, Math.min(1.0f, v));

        float c = vClamped * sClamped;
        float x = c * (1.0f - Math.abs((hNormalized / 60.0f) % 2.0f - 1.0f));
        float m = vClamped - c;

        float r1, g1, b1;

        if (hNormalized < 60.0f - PRECISION_EPSILON) {
            r1 = c; g1 = x; b1 = 0;
        } else if (hNormalized < 120.0f - PRECISION_EPSILON) {
            r1 = x; g1 = c; b1 = 0;
        } else if (hNormalized < 180.0f - PRECISION_EPSILON) {
            r1 = 0; g1 = c; b1 = x;
        } else if (hNormalized < 240.0f - PRECISION_EPSILON) {
            r1 = 0; g1 = x; b1 = c;
        } else if (hNormalized < 300.0f - PRECISION_EPSILON) {
            r1 = x; g1 = 0; b1 = c;
        } else {
            r1 = c; g1 = 0; b1 = x;
        }

        int r = clampToByte((r1 + m) * 255.0f);
        int g = clampToByte((g1 + m) * 255.0f);
        int b = clampToByte((b1 + m) * 255.0f);

        return new int[]{r, g, b};
    }

    private static int clampToByte(float value) {
        int result = (int) Math.round(value);
        if (result < 0) return 0;
        if (result > 255) return 255;
        return result;
    }

    private void addFieldListener(JTextField field, JSlider slider, int min, int max) {
        field.addActionListener(e -> {
            if (updating) return;
            try {
                int value = Integer.parseInt(field.getText().trim());
                if (value < min) value = min;
                if (value > max) value = max;
                slider.setValue(value);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Введите число от " + min + " до " + max,
                        "Ошибка ввода", JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    private void addCmykFieldListener(JTextField field, JSlider slider, int min, int max) {
        field.addActionListener(e -> {
            if (updating) return;
            try {
                int value = Integer.parseInt(field.getText().trim());
                if (value < min) value = min;
                if (value > max) value = max;
                slider.setValue(value);

                updating = true;
                updateFromCMYK();
                updating = false;
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Введите число от " + min + " до " + max,
                        "Ошибка ввода", JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    private void addHsvFieldListener(JTextField field, JSlider slider, int min, int max) {
        field.addActionListener(e -> {
            if (updating) return;
            try {
                int value = Integer.parseInt(field.getText().trim());
                if (value < min) value = min;
                if (value > max) value = max;
                slider.setValue(value);

                updating = true;
                updateFromHSV();
                updating = false;
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Введите число от " + min + " до " + max,
                        "Ошибка ввода", JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}