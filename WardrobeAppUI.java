import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;

public class WardrobeAppUI extends JFrame {
    static class ClothingItem {
        String name, category, size, color;
        ImageIcon image;
        String season;

        public ClothingItem(String name, String category, String size, String color, ImageIcon image, String season) {
            this.name = name;
            this.category = category;
            this.size = size;
            this.color = color;
            this.image = image;
            this.season = season;
        }

        @Override
        public String toString() {
            return name + " (" + category + ", " + color + ")";
        }
    }

    private final List<ClothingItem> wardrobe = new ArrayList<>();
    private final List<ClothingItem> currentOutfit = new ArrayList<>();
    private String currentSeason = "Fall";
    private int rotationAngle = 0;
    private boolean isRotating = false;
    private Timer rotationTimer;
    private final List<List<ClothingItem>> suggestedOutfits = new ArrayList<>();

    private final JPanel wardrobePanel = new JPanel(new GridLayout(0, 4, 10, 10));
    private final JPanel outfitPanel = new JPanel(new BorderLayout());
    private final JTextArea adviceArea = new JTextArea("Fashion advice will appear here...");
    private final JPanel mannequinPanel = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            // Draw mannequin base
            g2d.setColor(new Color(240, 240, 240));
            g2d.fillOval(100, 50, 100, 120); // Head
            g2d.fillRect(125, 170, 50, 100);  // Body

            // Apply rotation
            g2d.rotate(Math.toRadians(rotationAngle), 150, 220);

            // Draw current outfit on mannequin
            if (!currentOutfit.isEmpty()) {
                // Special handling for dresses - draw them larger
                boolean hasDress = currentOutfit.stream().anyMatch(item -> item.category.equals("Dresses"));
                
                for (ClothingItem item : currentOutfit) {
                    int width = 80;
                    int height = 80;
                    int x = 110;
                    int y = 100;
                    
                    // Adjust size and position for dresses
                    if (item.category.equals("Dresses")) {
                        width = 120;
                        height = 150;
                        x = 90;
                        y = 80;
                    }
                    
                    Image img = item.image.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                    g2d.drawImage(img, x, y, this);
                }
            }
        }
    };

    private final DefaultListModel<String> outfitListModel = new DefaultListModel<>();
    private final JList<String> outfitList = new JList<>(outfitListModel);

    public WardrobeAppUI() {
        setTitle("Virtual Wardrobe");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 800);
        setMinimumSize(new Dimension(800, 600)); // Set minimum window size
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        setResizable(true); // Enable window resizing

        Font font = new Font("SansSerif", Font.PLAIN, 13);

        askSeasonOnStartup();

        JMenuBar menuBar = new JMenuBar();
        JMenu settingsMenu = new JMenu("Settings");
        JMenuItem seasonMenuItem = new JMenuItem("Change Season");
        seasonMenuItem.addActionListener(e -> changeSeason());
        settingsMenu.add(seasonMenuItem);
        menuBar.add(settingsMenu);
        setJMenuBar(menuBar);

        // Create main panels
        JPanel leftPanel = new JPanel(new BorderLayout());
        JPanel centerPanel = new JPanel(new BorderLayout());
        JPanel rightPanel = new JPanel(new BorderLayout());

        // Upload Panel with resizable components
        JPanel uploadPanel = createUploadPanel(font);
        leftPanel.add(uploadPanel, BorderLayout.NORTH);

        // Wardrobe Panel
        JPanel wardrobeContainer = new JPanel(new BorderLayout());
        wardrobeContainer.setBorder(BorderFactory.createTitledBorder("Wardrobe"));
        JScrollPane scroll = new JScrollPane(wardrobePanel);
        wardrobeContainer.add(scroll, BorderLayout.CENTER);
        leftPanel.add(wardrobeContainer, BorderLayout.CENTER);

        // Mannequin Panel
        mannequinPanel.setPreferredSize(new Dimension(300, 400));
        mannequinPanel.setBorder(BorderFactory.createTitledBorder("Mannequin"));
        centerPanel.add(mannequinPanel, BorderLayout.CENTER);

        // Rotation controls
        JPanel rotationPanel = new JPanel();
        JButton startRotationBtn = createButton("▶ Start Rotation", font);
        JButton stopRotationBtn = createButton("⏹ Stop Rotation", font);

        startRotationBtn.addActionListener(e -> startRotation());
        stopRotationBtn.addActionListener(e -> stopRotation());

        rotationPanel.add(startRotationBtn);
        rotationPanel.add(stopRotationBtn);
        centerPanel.add(rotationPanel, BorderLayout.SOUTH);

        // Outfit Panel
        outfitPanel.setPreferredSize(new Dimension(300, 150));
        outfitPanel.setBorder(BorderFactory.createTitledBorder("Saved Outfit"));

        outfitList.setFont(font);
        JScrollPane outfitScrollPane = new JScrollPane(outfitList);
        outfitPanel.add(outfitScrollPane, BorderLayout.CENTER);

        JButton saveOutfitBtn = createButton("💾 Save", font);
        outfitPanel.add(saveOutfitBtn, BorderLayout.SOUTH);

        // Enhanced Advice Panel with seasonal tips
        JPanel advicePanel = new JPanel(new BorderLayout());
        advicePanel.setBorder(BorderFactory.createTitledBorder("Fashion Advice"));
        adviceArea.setFont(font);
        adviceArea.setLineWrap(true);
        adviceArea.setWrapStyleWord(true);
        adviceArea.setEditable(false);
        adviceArea.setPreferredSize(new Dimension(100, 40));
        advicePanel.add(new JScrollPane(adviceArea), BorderLayout.CENTER);

        // Suggestions Panel
        JPanel suggestionsPanel = new JPanel(new BorderLayout());
        suggestionsPanel.setBorder(BorderFactory.createTitledBorder("Suggestions"));
        JButton generateSuggestionsBtn = createButton("✨ Generate Suggestions", font);
        generateSuggestionsBtn.addActionListener(e -> generateSuggestions());
        suggestionsPanel.add(generateSuggestionsBtn, BorderLayout.NORTH);

        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        bottomPanel.add(outfitPanel);
        bottomPanel.add(advicePanel);

        rightPanel.add(suggestionsPanel, BorderLayout.NORTH);
        rightPanel.add(bottomPanel, BorderLayout.CENTER);

        // Add main panels to frame
        add(leftPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        refreshWardrobe();
        updateOutfitList();
        updateSeasonAdvice();

        setVisible(true);
    }

    private JPanel createUploadPanel(Font font) {
        JPanel uploadPanel = new JPanel(new GridBagLayout());
        uploadPanel.setBorder(BorderFactory.createTitledBorder("Add Clothing Item"));
        uploadPanel.setFont(font);

        // Make text fields resizable
        JTextField nameField = new JTextField(15);
        JComboBox<String> categoryBox = new JComboBox<>(new String[]{"Tops", "Bottoms", "Dresses", "Shoes", "Accessories"});
        JTextField sizeField = new JTextField(5);
        JTextField colorField = new JTextField(10);
        JComboBox<String> seasonBox = new JComboBox<>(new String[]{"Spring", "Summer", "Fall", "Winter", "All Seasons"});

        // Set resizable properties
        nameField.setPreferredSize(new Dimension(200, 25));
        nameField.setMinimumSize(new Dimension(150, 25));
        nameField.setMaximumSize(new Dimension(300, 25));
        
        sizeField.setPreferredSize(new Dimension(80, 25));
        colorField.setPreferredSize(new Dimension(120, 25));
        
        categoryBox.setFont(font);
        categoryBox.setPreferredSize(new Dimension(150, 25));
        categoryBox.setMaximumRowCount(5);
        seasonBox.setFont(font);
        seasonBox.setPreferredSize(new Dimension(150, 25));

        JLabel imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(100, 100));
        JButton imageBtn = createButton("📷 Image", font);
        JButton addBtn = createButton("➕ Add", font);
        JButton dailyOutfitBtn = createButton("🎯 Daily Outfit", font);

        // Set button sizes
        addBtn.setPreferredSize(new Dimension(120, 30));
        dailyOutfitBtn.setPreferredSize(new Dimension(150, 30));
        imageBtn.setPreferredSize(new Dimension(120, 30));

        final ImageIcon[] chosenImage = {null};
        imageBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                chosenImage[0] = new ImageIcon(chooser.getSelectedFile().getAbsolutePath());
                Image scaledImage = chosenImage[0].getImage().getScaledInstance(
                    imageLabel.getWidth(), imageLabel.getHeight(), Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaledImage));
            }
        });

        addBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String category = (String) categoryBox.getSelectedItem();
            String size = sizeField.getText().trim();
            String color = colorField.getText().trim();
            String season = (String) seasonBox.getSelectedItem();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name is required.");
                return;
            }
            ImageIcon image = chosenImage[0] != null ? chosenImage[0] : 
                new ImageIcon(new BufferedImage(60, 60, BufferedImage.TYPE_INT_RGB));
            ClothingItem newItem = new ClothingItem(name, category, size, color, image, season);
            wardrobe.add(newItem);
            refreshWardrobe();
            nameField.setText("");
            sizeField.setText("");
            colorField.setText("");
            chosenImage[0] = null;
            imageLabel.setIcon(null);
        });

        dailyOutfitBtn.addActionListener(e -> {
            generateDailyOutfit();
            adviceArea.setText("Daily outfit generated for " + currentSeason + "!\n\n" + getSeasonalAdvice(currentSeason));
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Add components with resizable properties
        String[] labels = {"Name:", "Category:", "Size:", "Color:", "Season:"};
        Component[] inputs = {nameField, categoryBox, sizeField, colorField, seasonBox};

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0;
            uploadPanel.add(createLabel(labels[i], font), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            uploadPanel.add(inputs[i], gbc);
        }

        // Image section
        gbc.gridx = 0;
        gbc.gridy = labels.length;
        gbc.gridwidth = 1;
        uploadPanel.add(imageBtn, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        uploadPanel.add(imageLabel, gbc);

        // Buttons row
        gbc.gridx = 0;
        gbc.gridy = labels.length + 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 5));
        buttonPanel.add(addBtn);
        buttonPanel.add(dailyOutfitBtn);
        uploadPanel.add(buttonPanel, gbc);

        // Make the panel itself resizable
        uploadPanel.setMinimumSize(new Dimension(300, 300));
        uploadPanel.setPreferredSize(new Dimension(400, 350));

        return uploadPanel;
    }

    private void startRotation() {
        if (suggestedOutfits.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Generate outfit suggestions first!");
            return;
        }

        if (!isRotating) {
            isRotating = true;
            rotationTimer = new Timer();
            rotationTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    rotationAngle = (rotationAngle + 5) % 360;
                    mannequinPanel.repaint();

                    if (rotationAngle == 0) {
                        SwingUtilities.invokeLater(() -> {
                            if (!suggestedOutfits.isEmpty()) {
                                currentOutfit.clear();
                                currentOutfit.addAll(suggestedOutfits.remove(0));
                                updateOutfitList();
                            } else {
                                stopRotation();
                            }
                        });
                    }
                }
            }, 0, 50);
        }
    }

    private void stopRotation() {
        if (isRotating) {
            rotationTimer.cancel();
            isRotating = false;
        }
    }

    private void generateSuggestions() {
        suggestedOutfits.clear();
        Random rand = new Random();

        for (int i = 0; i < 5; i++) {
            List<ClothingItem> tops = new ArrayList<>();
            List<ClothingItem> bottoms = new ArrayList<>();
            List<ClothingItem> dresses = new ArrayList<>();
            List<ClothingItem> shoes = new ArrayList<>();
            List<ClothingItem> accessories = new ArrayList<>();

            for (ClothingItem item : wardrobe) {
                if (item.season.equals(currentSeason) || item.season.equals("All Seasons")) {
                    switch (item.category) {
                        case "Tops": tops.add(item); break;
                        case "Bottoms": bottoms.add(item); break;
                        case "Dresses": dresses.add(item); break;
                        case "Shoes": shoes.add(item); break;
                        case "Accessories": accessories.add(item); break;
                    }
                }
            }

            List<ClothingItem> outfit = new ArrayList<>();
            if (!dresses.isEmpty() && rand.nextBoolean()) {
                outfit.add(dresses.get(rand.nextInt(dresses.size())));
            } else {
                if (!tops.isEmpty()) outfit.add(tops.get(rand.nextInt(tops.size())));
                if (!bottoms.isEmpty()) outfit.add(bottoms.get(rand.nextInt(bottoms.size())));
            }
            if (!shoes.isEmpty()) outfit.add(shoes.get(rand.nextInt(shoes.size())));
            if (!accessories.isEmpty() && rand.nextBoolean()) {
                outfit.add(accessories.get(rand.nextInt(accessories.size())));
            }

            if (!outfit.isEmpty()) {
                suggestedOutfits.add(outfit);
            }
        }

        if (!suggestedOutfits.isEmpty()) {
            currentOutfit.clear();
            currentOutfit.addAll(suggestedOutfits.get(0));
            updateOutfitList();
            adviceArea.setText("5 suggestions generated for " + currentSeason + "!\n\n" + getSeasonalAdvice(currentSeason));
            JOptionPane.showMessageDialog(this, "5 suggestions generated! Start rotation to see the outfits.");
        } else {
            JOptionPane.showMessageDialog(this, "Not enough clothing items to generate suggestions.");
        }
    }

    private void generateDailyOutfit() {
        currentOutfit.clear();
        Random rand = new Random();

        List<ClothingItem> tops = new ArrayList<>();
        List<ClothingItem> bottoms = new ArrayList<>();
        List<ClothingItem> dresses = new ArrayList<>();
        List<ClothingItem> shoes = new ArrayList<>();
        List<ClothingItem> accessories = new ArrayList<>();

        for (ClothingItem item : wardrobe) {
            if (item.season.equals(currentSeason) || item.season.equals("All Seasons")) {
                switch (item.category) {
                    case "Tops": tops.add(item); break;
                    case "Bottoms": bottoms.add(item); break;
                    case "Dresses": dresses.add(item); break;
                    case "Shoes": shoes.add(item); break;
                    case "Accessories": accessories.add(item); break;
                }
            }
        }

        if (!dresses.isEmpty()) {
            currentOutfit.add(dresses.get(rand.nextInt(dresses.size())));
        } else {
            if (!tops.isEmpty()) currentOutfit.add(tops.get(rand.nextInt(tops.size())));
            if (!bottoms.isEmpty()) currentOutfit.add(bottoms.get(rand.nextInt(bottoms.size())));
        }
        if (!shoes.isEmpty()) currentOutfit.add(shoes.get(rand.nextInt(shoes.size())));
        if (!accessories.isEmpty() && rand.nextBoolean()) {
            currentOutfit.add(accessories.get(rand.nextInt(accessories.size())));
        }

        updateOutfitList();
    }

    private void refreshWardrobe() {
        wardrobePanel.removeAll();
        for (ClothingItem item : wardrobe) {
            JPanel card = new JPanel(new BorderLayout(5, 5));
            card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

            Image scaledImg = item.image.getImage().getScaledInstance(140, 140, Image.SCALE_SMOOTH);
            JLabel img = new JLabel(new ImageIcon(scaledImg));
            img.setHorizontalAlignment(JLabel.CENTER);
            img.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

            JLabel label = new JLabel("<html><center>" + item.name + "<br/>" + item.category +
                    "<br/>Size: " + item.size + "<br/>Season: " + item.season + "</center></html>", JLabel.CENTER);
            label.setFont(new Font("SansSerif", Font.PLAIN, 13));

            JButton edit = createButton("✏️", label.getFont());
            JButton delete = createButton("❌", label.getFont());

            edit.addActionListener(e -> showEditDialog(item));
            delete.addActionListener(e -> {
                wardrobe.remove(item);
                refreshWardrobe();
                currentOutfit.remove(item);
                updateOutfitList();
            });

            JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
            btns.add(edit);
            btns.add(delete);

            card.add(img, BorderLayout.NORTH);
            card.add(label, BorderLayout.CENTER);
            card.add(btns, BorderLayout.SOUTH);

            card.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!currentOutfit.contains(item)) {
                        currentOutfit.add(item);
                        updateOutfitList();
                    }
                }
            });

            wardrobePanel.add(card);
        }
        wardrobePanel.revalidate();
        wardrobePanel.repaint();
    }

    private void showEditDialog(ClothingItem item) {
        JDialog dialog = new JDialog(this, "Edit Clothing Item", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        Font font = new Font("SansSerif", Font.PLAIN, 13);

        JTextField nameField = new JTextField(item.name);
        JTextField sizeField = new JTextField(item.size);
        JTextField colorField = new JTextField(item.color);
        JComboBox<String> seasonBox = new JComboBox<>(
                new String[]{"Spring", "Summer", "Fall", "Winter", "All Seasons"});
        seasonBox.setSelectedItem(item.season);

        JLabel imageLabel = new JLabel(new ImageIcon(item.image.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH)));
        JButton imageBtn = createButton("📷 Change Image", font);
        JButton saveBtn = createButton("✅ Save", font);

        final ImageIcon[] updatedImage = {item.image};

        imageBtn.addActionListener(ev -> {
            JFileChooser chooser = new JFileChooser();
            int result = chooser.showOpenDialog(dialog);
            if (result == JFileChooser.APPROVE_OPTION) {
                updatedImage[0] = new ImageIcon(chooser.getSelectedFile().getAbsolutePath());
                imageLabel.setIcon(new ImageIcon(updatedImage[0].getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH)));
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String[] labels = {"Name:", "Size:", "Color:", "Season:"};
        Component[] fields = {nameField, sizeField, colorField, seasonBox};

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            dialog.add(new JLabel(labels[i]), gbc);

            gbc.gridx = 1;
            dialog.add(fields[i], gbc);
        }

        gbc.gridx = 0;
        gbc.gridy = labels.length;
        dialog.add(imageBtn, gbc);

        gbc.gridx = 1;
        dialog.add(imageLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = labels.length + 1;
        gbc.gridwidth = 2;
        dialog.add(saveBtn, gbc);

        saveBtn.addActionListener(ev -> {
            item.name = nameField.getText().trim();
            item.size = sizeField.getText().trim();
            item.color = colorField.getText().trim();
            item.season = (String) seasonBox.getSelectedItem();
            item.image = updatedImage[0];
            refreshWardrobe();
            updateOutfitList();
            dialog.dispose();
        });

        dialog.setVisible(true);
    }

    private void updateOutfitList() {
        outfitListModel.clear();
        for (ClothingItem item : currentOutfit) {
            outfitListModel.addElement(item.toString());
        }
        mannequinPanel.repaint();
    }

    private void askSeasonOnStartup() {
        String[] seasons = {"Spring", "Summer", "Fall", "Winter"};
        String selectedSeason = (String) JOptionPane.showInputDialog(
                this,
                "What is the current season?",
                "Season Selection",
                JOptionPane.QUESTION_MESSAGE,
                null,
                seasons,
                seasons[0]);

        if (selectedSeason != null) {
            currentSeason = selectedSeason;
            updateSeasonAdvice();
        }
    }

    private void changeSeason() {
        String[] seasons = {"Spring", "Summer", "Fall", "Winter"};
        String selectedSeason = (String) JOptionPane.showInputDialog(
                this,
                "Change current season:",
                "Season Update",
                JOptionPane.QUESTION_MESSAGE,
                null,
                seasons,
                currentSeason);

        if (selectedSeason != null) {
            currentSeason = selectedSeason;
            updateSeasonAdvice();
            JOptionPane.showMessageDialog(this, "Season updated: " + currentSeason);
        }
    }

    private String getSeasonalAdvice(String season) {
        switch (season) {
            case "Spring":
                return "Spring Fashion Tips:\n\n" +
                       "• Light layers for changing temperatures\n" +
                       "• Pastel colors (pink, mint, lavender)\n" +
                       "• Light jackets or cardigans\n" +
                       "• Closed-toe shoes or ballet flats\n" +
                       "• Floral patterns\n" +
                       "• Light scarves for chilly mornings\n" +
                       "• Denim jackets for casual looks\n" +
                       "• Trench coats for rainy days";
            case "Summer":
                return "Summer Fashion Tips:\n\n" +
                       "• Light, breathable fabrics (linen, cotton)\n" +
                       "• Bright, vibrant colors\n" +
                       "• Wide-brimmed hats and sunglasses\n" +
                       "• Open sandals or espadrilles\n" +
                       "• Short sleeves and sleeveless tops\n" +
                       "• Lightweight dresses and skirts\n" +
                       "• Swimwear cover-ups\n" +
                       "• Light-colored clothing to reflect sunlight";
            case "Fall":
                return "Fall Fashion Tips:\n\n" +
                       "• Medium-weight layers\n" +
                       "• Warm colors (burgundy, mustard, olive)\n" +
                       "• Leather jackets or denim jackets\n" +
                       "• Ankle boots or loafers\n" +
                       "• Scarves and light gloves\n" +
                       "• Plaid patterns and knits\n" +
                       "• Turtlenecks and sweaters\n" +
                       "• Corduroy pants for texture";
            case "Winter":
                return "Winter Fashion Tips:\n\n" +
                       "• Heavy layers for warmth\n" +
                       "• Dark or neutral colors (black, gray, navy)\n" +
                       "• Insulated coats and parkas\n" +
                       "• Thermal underlayers\n" +
                       "• Wool socks and insulated boots\n" +
                       "• Chunky knit sweaters and scarves\n" +
                       "• Fleece-lined leggings\n" +
                       "• Waterproof outerwear for snow";
            default:
                return "Select a season to get fashion advice";
        }
    }

    private void updateSeasonAdvice() {
        adviceArea.setText(getSeasonalAdvice(currentSeason));
    }

    private JTextField createTextField(Font font) {
        JTextField tf = new JTextField();
        tf.setFont(font);
        tf.setPreferredSize(new Dimension(80, 25));
        return tf;
    }

    private JButton createButton(String text, Font font) {
        JButton btn = new JButton(text);
        btn.setFont(font);
        btn.setPreferredSize(new Dimension(90, 30));
        return btn;
    }

    private JLabel createLabel(String text, Font font) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(font);
        return lbl;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WardrobeAppUI::new);
    }
}