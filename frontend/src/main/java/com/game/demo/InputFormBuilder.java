package com.game.demo;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Map;

public class InputFormBuilder {
    public static void buildFormFromColumns(
            JPanel inputPanel,
            Map<String, JComponent> inputFields,
            String[] columns,
            String tableName,
            Map<Object, String> monsterNameCache,
            Map<Object, String> itemNameCache,
            Map<Object, String> skillNameCache,
            LayoutManager parent,
            Map<String, String> displayNames
    ) {
        inputPanel.removeAll();
        inputPanel.setLayout(new GridBagLayout());
        inputFields.clear();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        for (String column : columns) {
            JComponent input = null;

            // MONSTER
            if (tableName.equalsIgnoreCase("monster")) {
                if (column.equalsIgnoreCase("name")) {
                    JComboBox<String> combo = new JComboBox<>(parent.getEnemyFolderNames());
                    combo.setPreferredSize(new Dimension(200, 25));
                    input = combo;
                } else if (column.equalsIgnoreCase("behavior")) {
                    JComboBox<String> combo = new JComboBox<>(new String[]{"cận chiến", "tầm xa", "canh gác"});
                    combo.setPreferredSize(new Dimension(200, 25));
                    input = combo;
                }
            }
            // MONSTERDROP
            else if (tableName.equalsIgnoreCase("monsterdrop")) {
                if (column.equalsIgnoreCase("monsterId")) {
                    DefaultComboBoxModel<LayoutManager.ComboItem> model = new DefaultComboBoxModel<>();
                    for (var entry : monsterNameCache.entrySet()) {
                        model.addElement(new LayoutManager.ComboItem(entry.getKey(), entry.getValue()));
                    }
                    JComboBox<LayoutManager.ComboItem> combo = new JComboBox<>(model);
                    input = combo;
                } else if (column.equalsIgnoreCase("itemId")) {
                    DefaultComboBoxModel<LayoutManager.ComboItem> model = new DefaultComboBoxModel<>();
                    for (var entry : itemNameCache.entrySet()) {
                        model.addElement(new LayoutManager.ComboItem(entry.getKey(), entry.getValue()));
                    }
                    JComboBox<LayoutManager.ComboItem> combo = new JComboBox<>(model);
                    input = combo;
                }
            }
            // MAP
            else if (tableName.equalsIgnoreCase("map")) {
                if (column.equalsIgnoreCase("background") || column.equalsIgnoreCase("collisionlayer")
                        || column.equalsIgnoreCase("preview")) {
                    JPanel panel = new JPanel(new BorderLayout());
                    JTextField pathField = new JTextField();
                    JButton btn = new JButton("Chọn ảnh");
                    JLabel preview = new JLabel();

                    File rootDir = new File("src/main/resources").getAbsoluteFile();

                    btn.addActionListener(e -> {
                        JFileChooser chooser = new JFileChooser(new File(rootDir, "assets")) {
                            @Override
                            public void approveSelection() {
                                File selectedFile = getSelectedFile();
                                if (selectedFile != null && !selectedFile.getAbsolutePath().startsWith(rootDir.getAbsolutePath())) {
                                    JOptionPane.showMessageDialog(this,
                                            "Chỉ được chọn file bên trong thư mục: " + rootDir.getAbsolutePath(),
                                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                                super.approveSelection();
                            }
                        };
                        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PNG Images", "png"));

                        JLabel imagePreview = new JLabel();
                        imagePreview.setPreferredSize(new Dimension(200, 200));
                        chooser.setAccessory(imagePreview);

                        chooser.addPropertyChangeListener(evt -> {
                            if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
                                File selectedFile = (File) evt.getNewValue();
                                if (selectedFile != null && selectedFile.exists() && selectedFile.getName().toLowerCase().endsWith(".png")) {
                                    ImageIcon icon = new ImageIcon(selectedFile.getAbsolutePath());
                                    Image scaled = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                                    imagePreview.setIcon(new ImageIcon(scaled));
                                } else {
                                    imagePreview.setIcon(null);
                                }
                            }
                        });

                        int result = chooser.showOpenDialog(null);
                        if (result == JFileChooser.APPROVE_OPTION) {
                            File file = chooser.getSelectedFile();
                            String fullPath = file.getAbsolutePath();
                            String basePath = rootDir.getAbsolutePath();
                            String relativePath = fullPath.substring(basePath.length() + 1).replace("\\", "/");
                            pathField.setText(relativePath);
                            ImageIcon icon = new ImageIcon(file.getAbsolutePath());
                            Image scaled = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                            preview.setIcon(new ImageIcon(scaled));
                            panel.revalidate();
                        }
                    });

                    panel.add(pathField, BorderLayout.CENTER);
                    panel.add(btn, BorderLayout.EAST);
                    panel.add(preview, BorderLayout.SOUTH);
                    inputFields.put(column, pathField);
                    input = panel;
                } else if (column.equalsIgnoreCase("enemyId") || column.equalsIgnoreCase("bossId")) {
                    DefaultComboBoxModel<LayoutManager.ComboItem> model = new DefaultComboBoxModel<>();
                    for (var entry : monsterNameCache.entrySet()) {
                        model.addElement(new LayoutManager.ComboItem(entry.getKey(), entry.getValue()));
                    }
                    JComboBox<LayoutManager.ComboItem> combo = new JComboBox<>(model);
                    input = combo;
                }
            }
            // ITEM
            else if (tableName.equalsIgnoreCase("item")) {
                if (column.equalsIgnoreCase("icon")) {
                    JPanel panel = new JPanel(new BorderLayout());
                    JTextField pathField = new JTextField();
                    JButton btn = new JButton("Chọn ảnh");
                    JLabel preview = new JLabel();

                    File rootDir = new File("src/main/resources").getAbsoluteFile();

                    btn.addActionListener(e -> {
                        JFileChooser chooser = new JFileChooser(new File(rootDir, "assets/Item")) {
                            @Override
                            public void approveSelection() {
                                File selectedFile = getSelectedFile();
                                if (selectedFile != null && !selectedFile.getAbsolutePath().startsWith(rootDir.getAbsolutePath())) {
                                    JOptionPane.showMessageDialog(this,
                                            "Chỉ được chọn file bên trong thư mục: " + rootDir.getAbsolutePath(),
                                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                                super.approveSelection();
                            }
                        };
                        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PNG Images", "png"));

                        JLabel imagePreview = new JLabel();
                        imagePreview.setPreferredSize(new Dimension(200, 200));
                        chooser.setAccessory(imagePreview);

                        chooser.addPropertyChangeListener(evt -> {
                            if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
                                File selectedFile = (File) evt.getNewValue();
                                if (selectedFile != null && selectedFile.exists() && selectedFile.getName().toLowerCase().endsWith(".png")) {
                                    ImageIcon icon = new ImageIcon(selectedFile.getAbsolutePath());
                                    Image scaled = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                                    imagePreview.setIcon(new ImageIcon(scaled));
                                } else {
                                    imagePreview.setIcon(null);
                                }
                            }
                        });

                        int result = chooser.showOpenDialog(null);
                        if (result == JFileChooser.APPROVE_OPTION) {
                            File file = chooser.getSelectedFile();
                            String fullPath = file.getAbsolutePath();
                            String basePath = rootDir.getAbsolutePath();
                            String relativePath = fullPath.substring(basePath.length() + 1).replace("\\", "/");
                            pathField.setText(relativePath);
                            ImageIcon icon = new ImageIcon(file.getAbsolutePath());
                            Image scaled = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                            preview.setIcon(new ImageIcon(scaled));
                            panel.revalidate();
                        }
                    });

                    panel.add(pathField, BorderLayout.CENTER);
                    panel.add(btn, BorderLayout.EAST);
                    panel.add(preview, BorderLayout.SOUTH);
                    inputFields.put(column, pathField);
                    input = panel;
                }
            }
            // ITEMINSTANCE
            else if (tableName.equalsIgnoreCase("iteminstance")) {
                if (column.equalsIgnoreCase("itemId")) {
                    DefaultComboBoxModel<LayoutManager.ComboItem> model = new DefaultComboBoxModel<>();
                    for (var entry : itemNameCache.entrySet()) {
                        model.addElement(new LayoutManager.ComboItem(entry.getKey(), entry.getValue()));
                    }
                    JComboBox<LayoutManager.ComboItem> combo = new JComboBox<>(model);
                    input = combo;
                }
            }
            // SKILL
            else if (tableName.equalsIgnoreCase("skill")) {
                if (column.equalsIgnoreCase("icon")) {
                    JPanel panel = new JPanel(new BorderLayout());
                    JTextField pathField = new JTextField();
                    JButton btn = new JButton("Chọn ảnh");
                    JLabel preview = new JLabel();

                    File rootDir = new File("src/main/resources").getAbsoluteFile();

                    btn.addActionListener(e -> {
                        JFileChooser chooser = new JFileChooser(new File(rootDir, "assets")) {
                            @Override
                            public void approveSelection() {
                                File selectedFile = getSelectedFile();
                                if (selectedFile != null && !selectedFile.getAbsolutePath().startsWith(rootDir.getAbsolutePath())) {
                                    JOptionPane.showMessageDialog(this,
                                            "Chỉ được chọn file bên trong thư mục: " + rootDir.getAbsolutePath(),
                                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                                super.approveSelection();
                            }
                        };
                        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PNG Images", "png"));

                        JLabel imagePreview = new JLabel();
                        imagePreview.setPreferredSize(new Dimension(200, 200));
                        chooser.setAccessory(imagePreview);

                        chooser.addPropertyChangeListener(evt -> {
                            if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
                                File selectedFile = (File) evt.getNewValue();
                                if (selectedFile != null && selectedFile.exists() && selectedFile.getName().toLowerCase().endsWith(".png")) {
                                    ImageIcon icon = new ImageIcon(selectedFile.getAbsolutePath());
                                    Image scaled = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                                    imagePreview.setIcon(new ImageIcon(scaled));
                                } else {
                                    imagePreview.setIcon(null);
                                }
                            }
                        });

                        int result = chooser.showOpenDialog(null);
                        if (result == JFileChooser.APPROVE_OPTION) {
                            File file = chooser.getSelectedFile();
                            String fullPath = file.getAbsolutePath();
                            String basePath = rootDir.getAbsolutePath();
                            String relativePath = fullPath.substring(basePath.length() + 1).replace("\\", "/");
                            pathField.setText(relativePath);
                            ImageIcon icon = new ImageIcon(file.getAbsolutePath());
                            Image scaled = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                            preview.setIcon(new ImageIcon(scaled));
                            panel.revalidate();
                        }
                    });

                    panel.add(pathField, BorderLayout.CENTER);
                    panel.add(btn, BorderLayout.EAST);
                    panel.add(preview, BorderLayout.SOUTH);
                    inputFields.put(column, pathField);
                    input = panel;
                }
            }
            // SKILLUPDATE
            else if (tableName.equalsIgnoreCase("skillupdate")) {
                if (column.equalsIgnoreCase("skillId")) {
                    DefaultComboBoxModel<LayoutManager.ComboItem> model = new DefaultComboBoxModel<>();
                    for (var entry : skillNameCache.entrySet()) {
                        model.addElement(new LayoutManager.ComboItem(entry.getKey(), entry.getValue()));
                    }
                    JComboBox<LayoutManager.ComboItem> combo = new JComboBox<>(model);
                    input = combo;
                }
            }

            if (column.equalsIgnoreCase("id")) {
                JTextField field = new JTextField();
                field.setEditable(false);
                field.setPreferredSize(new Dimension(200, 25));
                input = field;
            } else if (column.equalsIgnoreCase("className")) {
                JComboBox<String> comboBox = new JComboBox<>(parent.Classer);
                comboBox.setPreferredSize(new Dimension(200, 25));
                input = comboBox;
            }

            // Nếu chưa có input đặc biệt thì dùng mặc định
            if (input == null) {
                JTextField field = new JTextField();
                field.setPreferredSize(new Dimension(200, 25));
                input = field;
            }

            if (input instanceof JTextField || input instanceof JComboBox) {
                inputFields.put(column, input);
            }
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.weightx = 0.5;
            String label = displayNames.getOrDefault(column, column);
            inputPanel.add(new JLabel(label + ":"), gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.5;
            inputPanel.add(input, gbc);

            row++;
        }

        inputPanel.revalidate();
        inputPanel.repaint();
    }
}