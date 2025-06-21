package com.game.demo;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.tree.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.data.GameData;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

import java.awt.*;
import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

public class LayoutManager extends JFrame {
    private JTable table;
    private JPanel inputPanel;
    private java.util.Map<String, JComponent> inputFields = new java.util.HashMap<>();
    public final String[] Classer = { "LangKhach", "Samurai", "Tanker", "Assassin", "Vampire" };

    private JScrollPane tableScroll;
    private JSplitPane verticalSplit;

    private JButton btnThem, btnSua, btnXoa;
    private JTextField searchField;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private TableSearchHelper tableSearchHelper;

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
        playerNode.add(new TableTreeNode("Tài khoản", "users"));
        playerNode.add(new TableTreeNode("Nhân vật","characters"));
        playerNode.add(new TableTreeNode("Kho kỹ năng","character_skills"));
        playerNode.add(new TableTreeNode("Kho đồ","inventory"));

        DefaultMutableTreeNode monsterNode = new DefaultMutableTreeNode("Monster");
        monsterNode.add(new TableTreeNode("Quái vật","monster"));
        monsterNode.add(new TableTreeNode("Quái vật rơi đồ","monsterdrop"));

        DefaultMutableTreeNode gameDataNode = new DefaultMutableTreeNode("GameData");
        gameDataNode.add(new TableTreeNode("Bản đồ","map"));

        DefaultMutableTreeNode itemNode = new DefaultMutableTreeNode("Item");
        itemNode.add(new TableTreeNode("Trang bị","item"));
        itemNode.add(new TableTreeNode("Trang bị cá nhân","iteminstance"));
        itemNode.add(new TableTreeNode("Thuộc tính","thuoctinh"));

        gameDataNode.add(itemNode);

        DefaultMutableTreeNode skillNode = new DefaultMutableTreeNode("Skill");
        skillNode.add(new TableTreeNode("Kỹ năng","skill"));
        skillNode.add(new TableTreeNode("Nâng cấp kỹ năng","skillupdate"));

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
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(33, 150, 243));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));

        tableSelect(table);

        // Tạo rowSorter cho table
        rowSorter = new TableRowSorter<>((DefaultTableModel) table.getModel());
        table.setRowSorter(rowSorter);

        tableSearchHelper = new TableSearchHelper(table, rowSorter, tableHelper);

        // Khung tìm kiếm
        searchField = new PlaceholderTextField(getSearchPlaceholder(""));
        searchField.setPreferredSize(new Dimension(200, 28));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setToolTipText("Tìm kiếm...");

        // Sự kiện lọc khi nhập
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { tableSearchHelper.filter(searchField.getText()); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { tableSearchHelper.filter(searchField.getText()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { tableSearchHelper.filter(searchField.getText()); }
        });

        // Panel chứa search + table
        JPanel tablePanel = new JPanel(new BorderLayout(5, 5));
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        searchPanel.add(new JLabel("🔍 "), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        tablePanel.add(searchPanel, BorderLayout.NORTH);
        tableScroll = new JScrollPane(table);
        tablePanel.add(tableScroll, BorderLayout.CENTER);

        // Input
        inputPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Thông tin"));
        inputPanel.add(new JLabel("Trống"));

        JScrollPane inputScroll = new JScrollPane(inputPanel);

        // Button
        JPanel buttonPanel = new JPanel();
        btnThem = new RoundedButton("Thêm", 15);
        btnSua = new RoundedButton("Sửa", 15);
        btnXoa = new RoundedButton("Xóa", 15);
        btnThem.addActionListener(e -> DataCrudHelper.addData(inputFields, tree, this));
        btnSua.addActionListener(e -> DataCrudHelper.updateData(inputFields, tree, this));
        btnXoa.addActionListener(e -> DataCrudHelper.deleteData(inputFields, tree, this));
        buttonPanel.add(btnThem);
        buttonPanel.add(btnSua);
        buttonPanel.add(btnXoa);

        JPanel formInput = new JPanel(new BorderLayout());
        formInput.add(inputScroll, BorderLayout.CENTER);
        formInput.add(buttonPanel, BorderLayout.SOUTH);

        // Áp dụng style cho các nút
        btnThem.setBackground(new Color(76, 175, 80));
        btnSua.setBackground(new Color(255, 193, 7));
        btnXoa.setBackground(new Color(244, 67, 54));
        btnThem.setForeground(Color.WHITE);
        btnSua.setForeground(Color.WHITE);
        btnXoa.setForeground(Color.WHITE);

        // Bottom Right: Table + Form
        verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tablePanel, formInput);
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
            if (selectedNode instanceof TableTreeNode) {
                String tableName = ((TableTreeNode) selectedNode).getTableName();
                if (tableName != null) {
                    if (!tableName.equals("Player") && !tableName.equals("Monster") && !tableName.equals("GameData")
                            && !tableName.equals("Item") && !tableName.equals("Skill")) {
                        try {
                            Properties props = new Properties();
                            props.load(getClass().getResourceAsStream("/app.properties"));
                            String secret = props.getProperty("admin.secret");

                            String url = "http://localhost:8080/api/" + tableName + "/";
                            HttpClient client = HttpClient.newHttpClient();
                            HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header("Authorization", "Bearer " + GameData.token)
                            .header("Xac-thuc", secret)
                            .GET()
                            .build();
                            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                            loadDataFromJson(response.body(), tableName);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this, "Bạn không có quyền truy cập!!!",
                                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });
    }

    // region Load table
    private void loadDataFromJson(String json, String tableName) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        java.util.List<java.util.Map<String, Object>> list = mapper.readValue(json, java.util.List.class);

        String[] columnNames;
        if (!list.isEmpty()) {
            columnNames = list.get(0).keySet().toArray(new String[0]);
        } else {
            columnNames = new String[] { "Trống" };
        }

        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        for (var row : list) {
            Object[] rowData = new Object[columnNames.length];
            for (int i = 0; i < columnNames.length; i++) {
                String col = columnNames[i];
                Object value = row.get(col);

                rowData[i] = value;
            }
            model.addRow(rowData);
        }

        table.setModel(model);
        rowSorter.setModel(model);
        table.setRowSorter(rowSorter);

        if (searchField instanceof PlaceholderTextField) {
            ((PlaceholderTextField) searchField).setPlaceholder(getSearchPlaceholder(tableName));
            searchField.repaint();
        }

        tableHelper.customizeTableDisplay(table, tableName);
        tableHelper.setColumnDisplayNames(table, tableName);
        InputFormBuilder.buildFormFromColumns(
                inputPanel, inputFields, columnNames, tableName,
                monsterNameCache, itemNameCache, skillNameCache, this, tableHelper.getDisplayNames());
        tableSearchHelper.setTableName(tableName);
    }

    public String[] getEnemyFolderNames() {
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
                        // Nếu là trường ảnh, cập nhật preview
                        if (columnName.equalsIgnoreCase("icon") || columnName.equalsIgnoreCase("background")
                                || columnName.equalsIgnoreCase("preview")
                                || columnName.equalsIgnoreCase("collisionlayer")) {
                            // Tìm panel cha chứa preview
                            Container parent = field.getParent();
                            for (Component comp : parent.getComponents()) {
                                if (comp instanceof JLabel) {
                                    JLabel preview = (JLabel) comp;
                                    String path = field.getText();
                                    if (path != null && !path.isEmpty()) {
                                        java.net.URL imgUrl = getClass().getClassLoader().getResource(path);
                                        if (imgUrl != null) {
                                            ImageIcon icon = new ImageIcon(imgUrl);
                                            Image img = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                                            preview.setIcon(new ImageIcon(img));
                                        } else {
                                            preview.setIcon(null);
                                        }
                                    } else {
                                        preview.setIcon(null);
                                    }
                                }
                            }
                        }
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

    public static class ComboItem {
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

    public class RoundedButton extends JButton {
        private final int radius;

        public RoundedButton(String text, int radius) {
            super(text);
            this.radius = radius;
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setFont(getFont().deriveFont(Font.BOLD, 12f));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isArmed() ? getBackground().darker() : getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            super.paintComponent(g);
            g2.dispose();
        }

        @Override
        public void updateUI() {
            super.updateUI();
            setOpaque(false);
        }
    }

    public class PlaceholderTextField extends JTextField {
        private String placeholder;

        public PlaceholderTextField(String placeholder) {
            this.placeholder = placeholder;
        }

        public void setPlaceholder(String placeholder) {
            this.placeholder = placeholder;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !isFocusOwner() && placeholder != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setFont(getFont().deriveFont(Font.ITALIC));
                g2.setColor(Color.GRAY);
                Insets insets = getInsets();
                g2.drawString(placeholder, insets.left + 4, getHeight() / 2 + getFont().getSize() / 2 - 2);
                g2.dispose();
            }
        }
    }

    public class TableTreeNode extends DefaultMutableTreeNode {
        private final String displayName;
        private final String tableName;

        public TableTreeNode(String displayName, String tableName) {
            super(displayName); // Hiển thị trên tree
            this.displayName = displayName;
            this.tableName = tableName;
        }

        public String getTableName() {
            return tableName;
        }
    }

    private String getSearchPlaceholder(String tableName) {
        switch (tableName.toLowerCase()) {
            case "users":
                return "Tìm kiếm theo tên người dùng, email...";
            case "characters":
                return "Tìm kiếm theo tên nhân vật, lớp nhân vật...";    
            case "item":
                return "Tìm kiếm theo tên hoặc loại vật phẩm...";
            case "monster":
                return "Tìm kiếm theo tên quái, hành vi...";
            case "map":
                return "Tìm kiếm theo tên bản đồ, level...";
            case "skill":
                return "Tìm kiếm theo tên kỹ năng, lớp nhân vật...";
            case "monsterdrop":
                return "Tìm kiếm theo tên quái, tên vật phẩm...";
            case "iteminstance":
                return "Tìm kiếm theo tên vật phẩm...";
            case "skillupdate":
                return "Tìm kiếm theo tên kỹ năng...";
            default:
                return "Tìm kiếm...";
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatIntelliJLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new LayoutManager().setVisible(true));
    }
}
