package com.game.demo;

import javax.swing.*;
import javax.swing.tree.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LayoutManager extends JFrame {
    private JTable table;
    private JPanel inputPanel;
    private java.util.Map<String, JComponent> inputFields = new java.util.HashMap<>();
    private final String[] Classer = {"LangKhach", "Samurai", "Tanker", "Assassin", "Vampire"};

    private JScrollPane tableScroll;
    private JSplitPane verticalSplit;

    private JButton btnThem, btnSua, btnXoa;

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
        table = new JTable(new DefaultTableModel(new Object[]{"Trống"}, 0));
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
    }

    private void treeSelect(JTree tree) {
        tree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode selectedNode = 
                (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (selectedNode == null || selectedNode.isRoot()) return;

            String tableName = selectedNode.toString();
            if(!tableName.equals("Player") && !tableName.equals("Monster") && !tableName.equals("GameData") && !tableName.equals("Item") && !tableName.equals("Skill")){
                try {
                    String url = "http://localhost:8080/api/" + tableName + "/";
                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    loadDataFromJson(response.body());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    // region Load table
    private void loadDataFromJson(String json) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        java.util.List<java.util.Map<String, Object>> list = 
            mapper.readValue(json, java.util.List.class);

        if (list.isEmpty()) return;

        String[] columnNames = list.get(0).keySet().toArray(new String[0]);
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        for (var row : list) {
            Object[] rowData = row.values().toArray();
            model.addRow(rowData);
        }

        table.setModel(model);
        buildFormFromColumns(columnNames);
    }

    // region Load input
    private void buildFormFromColumns(String[] columns) {
        inputPanel.removeAll();
        inputPanel.setLayout(new GridLayout(columns.length, 2, 5, 5));
        inputFields.clear();

        for (String column : columns) {
            inputPanel.add(new JLabel(column + ":"));
            JComponent input;

            if (column.equalsIgnoreCase("id")) {
                JTextField field = new JTextField();
                field.setEditable(false);
                field.setPreferredSize(new Dimension(200, 25));
                input = field;
            } else if (column.equalsIgnoreCase("className")) {
                JComboBox<String> comboBox = new JComboBox<>(Classer);
                comboBox.setPreferredSize(new Dimension(200, 25));
                input = comboBox;
            } else {
                JTextField field = new JTextField();
                field.setPreferredSize(new Dimension(200, 25));
                input = field;
            }

            inputFields.put(column, input);
            inputPanel.add(input);
        }

        inputPanel.revalidate();
        inputPanel.repaint();
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
                        JComboBox<String> combo = (JComboBox<String>) input;
                        combo.setSelectedItem(value != null ? value.toString() : "");
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
                        data.put(key, ((JComboBox<?>) comp).getSelectedItem());
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
                        data.put(key, ((JComboBox<?>) comp).getSelectedItem());
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

                int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xoá bản ghi này không?", "Xác nhận xoá", JOptionPane.YES_NO_OPTION);
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


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LayoutManager().setVisible(true));
    }
}
