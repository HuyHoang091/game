package com.game.demo;

import javax.swing.*;
import javax.swing.tree.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LayoutManager extends JFrame {
    private JTable table;
    private JPanel inputPanel;
    private java.util.Map<String, JComponent> inputFields = new java.util.HashMap<>();
    private final String[] Classer = { "LangKhach", "Samurai", "Tanker", "Assassin", "Vampire" };

    private JScrollPane tableScroll;
    private JSplitPane verticalSplit;

    private JButton btnThem, btnSua, btnXoa;

    // --- Cache cho lookup nhanh ---
    private final java.util.Map<Object, String> itemNameCache = new java.util.HashMap<>();
    private final java.util.Map<Object, String> itemIconCache = new java.util.HashMap<>();
    private final java.util.Map<Object, String> monsterNameCache = new java.util.HashMap<>();
    private final java.util.Map<Object, String> mapNameCache = new java.util.HashMap<>();
    private final java.util.Map<Object, String> skillNameCache = new java.util.HashMap<>();

    private final TableDisplayHelper tableHelper = new TableDisplayHelper(
            itemNameCache, itemIconCache, monsterNameCache, mapNameCache, skillNameCache);

    public LayoutManager() {
        setTitle("Quản Lý Dữ Liệu");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel titleLabel = new JLabel("QUẢN LÝ", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);

        // JTree
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("ROOT");

        DefaultMutableTreeNode playerNode = new DefaultMutableTreeNode("Player");
        playerNode.add(new DefaultMutableTreeNode("users"));
        playerNode.add(new DefaultMutableTreeNode("characters"));
        playerNode.add(new DefaultMutableTreeNode("character_skills"));
        playerNode.add(new DefaultMutableTreeNode("inventory"));

        DefaultMutableTreeNode monsterNode = new DefaultMutableTreeNode("Monster");
        monsterNode.add(new DefaultMutableTreeNode("monster"));
        monsterNode.add(new DefaultMutableTreeNode("monsterdrop"));

        DefaultMutableTreeNode gameDataNode = new DefaultMutableTreeNode("GameData");
        gameDataNode.add(new DefaultMutableTreeNode("map"));

        DefaultMutableTreeNode itemNode = new DefaultMutableTreeNode("Item");
        itemNode.add(new DefaultMutableTreeNode("item"));
        itemNode.add(new DefaultMutableTreeNode("iteminstance"));
        itemNode.add(new DefaultMutableTreeNode("thuoctinh"));

        gameDataNode.add(itemNode);

        DefaultMutableTreeNode skillNode = new DefaultMutableTreeNode("Skill");
        skillNode.add(new DefaultMutableTreeNode("skill"));
        skillNode.add(new DefaultMutableTreeNode("skillupdate"));

        gameDataNode.add(skillNode);

        root.add(playerNode);
        root.add(monsterNode);
        root.add(gameDataNode);

        JTree tree = new JTree(root);
        tree.setRootVisible(false);

        treeSelect(tree);

        JScrollPane treeScroll = new JScrollPane(tree);

        // Table
        table = new JTable(new DefaultTableModel(new Object[] { "Trống" }, 0));
        tableSelect(table);
        tableScroll = new JScrollPane(table);

        // Input
        inputPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Thông tin"));
        inputPanel.add(new JLabel("Trống"));

        JScrollPane inputScroll = new JScrollPane(inputPanel);

        // Button
        JPanel buttonPanel = new JPanel();
        btnThem = new JButton("Thêm");
        addData(btnThem, tree);
        btnSua = new JButton("Sửa");
        updateData(btnSua, tree);
        btnXoa = new JButton("Xóa");
        deleteData(btnXoa, tree);
        buttonPanel.add(btnThem);
        buttonPanel.add(btnSua);
        buttonPanel.add(btnXoa);

        JPanel formInput = new JPanel(new BorderLayout());
        formInput.add(inputScroll, BorderLayout.CENTER);
        formInput.add(buttonPanel, BorderLayout.SOUTH);

        // Bottom Right: Table + Form
        verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, formInput);
        verticalSplit.setResizeWeight(0.7); // 70% table, 30% form

        // Main Split: JTree + Right
        JSplitPane horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScroll, verticalSplit);
        horizontalSplit.setResizeWeight(0.2); // 20% tree, 80% right panel

        add(horizontalSplit, BorderLayout.CENTER);

        tableHelper.loadAllCaches(); // <-- Thêm dòng này để cache dữ liệu lookup
    }

    private void treeSelect(JTree tree) {
        tree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (selectedNode == null || selectedNode.isRoot())
                return;

            String tableName = selectedNode.toString();
            if (!tableName.equals("Player") && !tableName.equals("Monster") && !tableName.equals("GameData")
                    && !tableName.equals("Item") && !tableName.equals("Skill")) {
                try {
                    String url = "http://localhost:8080/api/" + tableName + "/";
                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    loadDataFromJson(response.body(), tableName);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    // region Load table
    private void loadDataFromJson(String json, String tableName) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        java.util.List<java.util.Map<String, Object>> list = mapper.readValue(json, java.util.List.class);

        if (list.isEmpty())
            return;

        String[] columnNames = list.get(0).keySet().toArray(new String[0]);
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        for (var row : list) {
            Object[] rowData = row.values().toArray();
            model.addRow(rowData);
        }

        table.setModel(model);
        buildFormFromColumns(columnNames, tableName);
        tableHelper.customizeTableDisplay(table, tableName);
    }

    // region Load input
    private void buildFormFromColumns(String[] columns, String tableName) {
        inputPanel.removeAll();
        inputPanel.setLayout(new GridLayout(columns.length, 2, 5, 5));
        inputFields.clear();

        for (String column : columns) {
            inputPanel.add(new JLabel(column + ":"));
            JComponent input = null;

            // MONSTER
            if (tableName.equalsIgnoreCase("monster")) {
                if (column.equalsIgnoreCase("name")) {
                    // Combobox tên thư mục trong assets/Enemy
                    JComboBox<String> combo = new JComboBox<>(getEnemyFolderNames());
                    combo.setPreferredSize(new Dimension(200, 25));
                    input = combo;
                } else if (column.equalsIgnoreCase("behavior")) {
                    JComboBox<String> combo = new JComboBox<>(new String[] { "cận chiến", "tầm xa", "canh gác" });
                    combo.setPreferredSize(new Dimension(200, 25));
                    input = combo;
                }
            }
            // MONSTERDROP
            else if (tableName.equalsIgnoreCase("monsterdrop")) {
                if (column.equalsIgnoreCase("monsterId")) {
                    DefaultComboBoxModel<ComboItem> model = new DefaultComboBoxModel<>();
                    for (var entry : monsterNameCache.entrySet()) {
                        model.addElement(new ComboItem(entry.getKey(), entry.getValue()));
                    }
                    JComboBox<ComboItem> combo = new JComboBox<>(model);
                    input = combo;
                } else if (column.equalsIgnoreCase("itemId")) {
                    DefaultComboBoxModel<ComboItem> model = new DefaultComboBoxModel<>();
                    for (var entry : itemNameCache.entrySet()) {
                        model.addElement(new ComboItem(entry.getKey(), entry.getValue()));
                    }
                    JComboBox<ComboItem> combo = new JComboBox<>(model);
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
                    btn.addActionListener(e -> {
                        JFileChooser chooser = new JFileChooser("assets/Map");
                        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PNG Images", "png"));
                        int result = chooser.showOpenDialog(this);
                        if (result == JFileChooser.APPROVE_OPTION) {
                            java.io.File file = chooser.getSelectedFile();
                            String relativePath = "assets/Map/" + file.getName();
                            pathField.setText(relativePath);
                            preview.setIcon(new ImageIcon(file.getAbsolutePath()));
                        }
                    });
                    panel.add(pathField, BorderLayout.CENTER);
                    panel.add(btn, BorderLayout.EAST);
                    panel.add(preview, BorderLayout.SOUTH);
                    input = panel;
                } else if (column.equalsIgnoreCase("enemyId") || column.equalsIgnoreCase("bossId")) {
                    DefaultComboBoxModel<ComboItem> model = new DefaultComboBoxModel<>();
                    for (var entry : monsterNameCache.entrySet()) {
                        model.addElement(new ComboItem(entry.getKey(), entry.getValue()));
                    }
                    JComboBox<ComboItem> combo = new JComboBox<>(model);
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

                    // Thư mục gốc (tính từ thư mục dự án)
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

                        // Tạo label để hiển thị ảnh thumbnail
                        JLabel imagePreview = new JLabel();
                        imagePreview.setPreferredSize(new Dimension(200, 200));
                        chooser.setAccessory(imagePreview);

                        // Cập nhật thumbnail mỗi khi chọn file mới
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

                            // Cắt đường dẫn tương đối sau "src/main/resources/"
                            String relativePath = fullPath.substring(basePath.length() + 1).replace("\\", "/");

                            pathField.setText(relativePath);
                            preview.setIcon(new ImageIcon(file.getAbsolutePath()));
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
                    DefaultComboBoxModel<ComboItem> model = new DefaultComboBoxModel<>();
                    for (var entry : itemNameCache.entrySet()) {
                        model.addElement(new ComboItem(entry.getKey(), entry.getValue()));
                    }
                    JComboBox<ComboItem> combo = new JComboBox<>(model);
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
                    btn.addActionListener(e -> {
                        JFileChooser chooser = new JFileChooser("assets/Icon");
                        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PNG Images", "png"));
                        int result = chooser.showOpenDialog(this);
                        if (result == JFileChooser.APPROVE_OPTION) {
                            java.io.File file = chooser.getSelectedFile();
                            String relativePath = "assets/Icon/" + file.getName();
                            pathField.setText(relativePath);
                            preview.setIcon(new ImageIcon(file.getAbsolutePath()));
                        }
                    });
                    panel.add(pathField, BorderLayout.CENTER);
                    panel.add(btn, BorderLayout.EAST);
                    panel.add(preview, BorderLayout.SOUTH);
                    input = panel;
                }
            }
            // SKILLUPDATE
            else if (tableName.equalsIgnoreCase("skillupdate")) {
                if (column.equalsIgnoreCase("skillId")) {
                    DefaultComboBoxModel<ComboItem> model = new DefaultComboBoxModel<>();
                    for (var entry : skillNameCache.entrySet()) {
                        model.addElement(new ComboItem(entry.getKey(), entry.getValue()));
                    }
                    JComboBox<ComboItem> combo = new JComboBox<>(model);
                    input = combo;
                }
            }

            if (column.equalsIgnoreCase("id")) {
                JTextField field = new JTextField();
                field.setEditable(false);
                field.setPreferredSize(new Dimension(200, 25));
                input = field;
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
            inputPanel.add(input);
        }

        inputPanel.revalidate();
        inputPanel.repaint();
    }

    private String[] getEnemyFolderNames() {
        try {
            java.net.URL url = getClass().getClassLoader().getResource("assets/Enemy");
            if (url != null && url.getProtocol().equals("file")) {
                java.io.File file = new java.io.File(url.toURI());
                String[] jarList = file.list((current, name) -> new java.io.File(current, name).isDirectory());
                return jarList != null ? jarList : new String[0];
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new String[0];
    }

    private void tableSelect(JTable table) {
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                for (int col = 0; col < model.getColumnCount(); col++) {
                    String columnName = model.getColumnName(col);
                    Object value = model.getValueAt(row, col);
                    JComponent input = inputFields.get(columnName);
                    if (input instanceof JTextField) {
                        JTextField field = (JTextField) input;
                        field.setText(value != null ? value.toString() : "");
                    } else if (input instanceof JComboBox) {
                        JComboBox<?> combo = (JComboBox<?>) input;
                        if (combo.getItemCount() > 0) {
                            for (int i = 0; i < combo.getItemCount(); i++) {
                                Object item = combo.getItemAt(i);
                                if (item instanceof ComboItem) {
                                    if (((ComboItem) item).getId().toString()
                                            .equals(value != null ? value.toString() : "")) {
                                        combo.setSelectedIndex(i);
                                        break;
                                    }
                                } else {
                                    if (item != null && item.toString().equals(value != null ? value.toString() : "")) {
                                        combo.setSelectedIndex(i);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    // region Thêm
    private void addData(JButton btnThem, JTree tree) {
        btnThem.addActionListener(e -> {
            try {
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                if (selectedNode == null || selectedNode.isRoot()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng chọn bảng dữ liệu trên cây.");
                    return;
                }

                String tableName = selectedNode.toString();

                java.util.Map<String, Object> data = new java.util.HashMap<>();
                for (var entry : inputFields.entrySet()) {
                    String key = entry.getKey();
                    JComponent comp = entry.getValue();
                    if (comp instanceof JTextField) {
                        data.put(key, ((JTextField) comp).getText());
                    } else if (comp instanceof JComboBox) {
                        Object selected = ((JComboBox<?>) comp).getSelectedItem();
                        if (selected instanceof ComboItem) {
                            data.put(key, ((ComboItem) selected).getId());
                        } else {
                            data.put(key, selected);
                        }
                    }
                }

                data.remove("id");

                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(data);

                String url = "http://localhost:8080/api/" + tableName + "/";

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JOptionPane.showMessageDialog(this, "Thêm mới thành công!");
                    if (selectedNode != null) {
                        TreePath path = new TreePath(selectedNode.getPath());
                        tree.setSelectionPath(null);
                        tree.setSelectionPath(path);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi thêm mới: " + response.body());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi gửi dữ liệu: " + ex.getMessage());
            }
        });
    }

    // region Sửa
    private void updateData(JButton btnUpdate, JTree tree) {
        btnUpdate.addActionListener(e -> {
            try {
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                if (selectedNode == null || selectedNode.isRoot()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng chọn bảng dữ liệu trên cây.");
                    return;
                }

                String tableName = selectedNode.toString();

                Object idValue = null;
                if (inputFields.containsKey("id")) {
                    JComponent idComp = inputFields.get("id");
                    if (idComp instanceof JTextField) {
                        idValue = ((JTextField) idComp).getText();
                    } else if (idComp instanceof JComboBox) {
                        idValue = ((JComboBox<?>) idComp).getSelectedItem();
                    }
                    if (idValue == null || idValue.toString().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Vui lòng chọn bản ghi để cập nhật.");
                        return;
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Không tìm thấy trường id.");
                    return;
                }

                java.util.Map<String, Object> data = new java.util.HashMap<>();
                for (var entry : inputFields.entrySet()) {
                    String key = entry.getKey();
                    JComponent comp = entry.getValue();
                    if (comp instanceof JTextField) {
                        data.put(key, ((JTextField) comp).getText());
                    } else if (comp instanceof JComboBox) {
                        Object selected = ((JComboBox<?>) comp).getSelectedItem();
                        if (selected instanceof ComboItem) {
                            data.put(key, ((ComboItem) selected).getId());
                        } else {
                            data.put(key, selected);
                        }
                    }
                }

                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(data);

                String url = "http://localhost:8080/api/" + tableName + "/" + idValue;

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JOptionPane.showMessageDialog(this, "Cập nhật thành công!");

                    if (selectedNode != null) {
                        TreePath path = new TreePath(selectedNode.getPath());
                        tree.setSelectionPath(null);
                        tree.setSelectionPath(path);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật: " + response.body());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi gửi dữ liệu: " + ex.getMessage());
            }
        });
    }

    // region Xóa
    private void deleteData(JButton btnDelete, JTree tree) {
        btnDelete.addActionListener(e -> {
            try {
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                if (selectedNode == null || selectedNode.isRoot()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng chọn bảng dữ liệu trên cây.");
                    return;
                }

                String tableName = selectedNode.toString();

                Object idValue = null;
                if (inputFields.containsKey("id")) {
                    JComponent idComp = inputFields.get("id");
                    if (idComp instanceof JTextField) {
                        idValue = ((JTextField) idComp).getText();
                    } else if (idComp instanceof JComboBox) {
                        idValue = ((JComboBox<?>) idComp).getSelectedItem();
                    }
                    if (idValue == null || idValue.toString().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Vui lòng chọn bản ghi để xoá.");
                        return;
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Không tìm thấy trường id.");
                    return;
                }

                int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xoá bản ghi này không?",
                        "Xác nhận xoá", JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }

                String url = "http://localhost:8080/api/" + tableName + "/" + idValue;

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .DELETE()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JOptionPane.showMessageDialog(this, "Xoá thành công!");

                    if (selectedNode != null) {
                        TreePath path = new TreePath(selectedNode.getPath());
                        tree.setSelectionPath(null);
                        tree.setSelectionPath(path);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi xoá: " + response.body());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi gửi dữ liệu: " + ex.getMessage());
            }
        });
    }

    private String selectImageFromResource(String resourceFolder) {
        JDialog dialog = new JDialog(this, "Chọn ảnh", true);
        JPanel panel = new JPanel(new FlowLayout());
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setPreferredSize(new Dimension(400, 300));

        java.net.URL url = getClass().getClassLoader().getResource(resourceFolder);
        if (url != null && url.getProtocol().equals("file")) {
            java.io.File dir = new java.io.File(url.getPath());
            String[] files = dir.list((d, name) -> name.endsWith(".png") || name.endsWith(".jpg"));
            if (files != null) {
                for (String file : files) {
                    java.net.URL imgUrl = getClass().getClassLoader().getResource(resourceFolder + "/" + file);
                    if (imgUrl != null) {
                        ImageIcon icon = new ImageIcon(imgUrl);
                        Image img = icon.getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH);
                        JLabel label = new JLabel(new ImageIcon(img));
                        label.setToolTipText(file);
                        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        label.addMouseListener(new java.awt.event.MouseAdapter() {
                            public void mouseClicked(java.awt.event.MouseEvent e) {
                                dialog.setTitle(file); // dùng title để trả về tên file
                                dialog.dispose();
                            }
                        });
                        panel.add(label);
                    }
                }
            }
        }
        dialog.add(scroll);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        String selected = dialog.getTitle();
        return (selected != null && !selected.equals("Chọn ảnh")) ? selected : null;
    }

    private static class ComboItem {
        private final Object id;
        private final String name;

        public ComboItem(Object id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        public Object getId() {
            return id;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LayoutManager().setVisible(true));
    }
}
