package com.game.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.data.GameData;
import com.game.demo.LayoutManager.TableTreeNode;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class DataCrudHelper {

    public static void addData(Map<String, JComponent> inputFields, JTree tree, JFrame parent) {
        try {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (selectedNode == null || selectedNode.isRoot()) {
                JOptionPane.showMessageDialog(parent, "Vui lòng chọn bảng dữ liệu trên cây.");
                return;
            }

            String tableName = ((TableTreeNode) selectedNode).getTableName();

            java.util.Map<String, Object> data = collectData(inputFields);

            data.remove("id");

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(data);

            String url = "http://localhost:8080/api/" + tableName + "/";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + GameData.token)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JOptionPane.showMessageDialog(parent, response.body());
                if (selectedNode != null) {
                    TreePath path = new TreePath(selectedNode.getPath());
                    tree.setSelectionPath(null);
                    tree.setSelectionPath(path);
                }
            } else {
                JOptionPane.showMessageDialog(parent, "Lỗi khi thêm mới: " + response.body());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Lỗi khi gửi dữ liệu: " + ex.getMessage());
        }
    }

    public static void updateData(Map<String, JComponent> inputFields, JTree tree, JFrame parent) {
        try {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (selectedNode == null || selectedNode.isRoot()) {
                JOptionPane.showMessageDialog(parent, "Vui lòng chọn bảng dữ liệu trên cây.");
                return;
            }

            String tableName = ((TableTreeNode) selectedNode).getTableName();

            Object idValue = null;
            if (inputFields.containsKey("id")) {
                JComponent idComp = inputFields.get("id");
                if (idComp instanceof JTextField) {
                    idValue = ((JTextField) idComp).getText();
                } else if (idComp instanceof JComboBox) {
                    idValue = ((JComboBox<?>) idComp).getSelectedItem();
                }
                if (idValue == null || idValue.toString().isEmpty()) {
                    JOptionPane.showMessageDialog(parent, "Vui lòng chọn bản ghi để cập nhật.");
                    return;
                }
            } else {
                JOptionPane.showMessageDialog(parent, "Không tìm thấy trường id.");
                return;
            }

            java.util.Map<String, Object> data = collectData(inputFields);

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(data);

            String url = "http://localhost:8080/api/" + tableName + "/" + idValue;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + GameData.token)
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JOptionPane.showMessageDialog(parent, response.body());
                if (selectedNode != null) {
                    TreePath path = new TreePath(selectedNode.getPath());
                    tree.setSelectionPath(null);
                    tree.setSelectionPath(path);
                }
            } else {
                JOptionPane.showMessageDialog(parent, "Lỗi khi cập nhật: " + response.body());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Lỗi khi gửi dữ liệu: " + ex.getMessage());
        }
    }

    public static void deleteData(Map<String, JComponent> inputFields, JTree tree, JFrame parent) {
        try {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (selectedNode == null || selectedNode.isRoot()) {
                JOptionPane.showMessageDialog(parent, "Vui lòng chọn bảng dữ liệu trên cây.");
                return;
            }

            String tableName = ((TableTreeNode) selectedNode).getTableName();

            Object idValue = null;
            if (inputFields.containsKey("id")) {
                JComponent idComp = inputFields.get("id");
                if (idComp instanceof JTextField) {
                    idValue = ((JTextField) idComp).getText();
                } else if (idComp instanceof JComboBox) {
                    idValue = ((JComboBox<?>) idComp).getSelectedItem();
                }
                if (idValue == null || idValue.toString().isEmpty()) {
                    JOptionPane.showMessageDialog(parent, "Vui lòng chọn bản ghi để xoá.");
                    return;
                }
            } else {
                JOptionPane.showMessageDialog(parent, "Không tìm thấy trường id.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(parent, "Bạn có chắc muốn xoá bản ghi này không?",
                    "Xác nhận xoá", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            String url = "http://localhost:8080/api/" + tableName + "/" + idValue;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + GameData.token)
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JOptionPane.showMessageDialog(parent, response.body());
                if (selectedNode != null) {
                    TreePath path = new TreePath(selectedNode.getPath());
                    tree.setSelectionPath(null);
                    tree.setSelectionPath(path);
                }
            } else {
                JOptionPane.showMessageDialog(parent, "Lỗi khi xoá: " + response.body());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Lỗi khi gửi dữ liệu: " + ex.getMessage());
        }
    }

    private static java.util.Map<String, Object> collectData(Map<String, JComponent> inputFields) {
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        for (var entry : inputFields.entrySet()) {
            String key = entry.getKey();
            JComponent comp = entry.getValue();
            if (comp instanceof JTextField) {
                data.put(key, ((JTextField) comp).getText());
            } else if (comp instanceof JComboBox) {
                Object selected = ((JComboBox<?>) comp).getSelectedItem();
                if (selected instanceof LayoutManager.ComboItem) {
                    data.put(key, ((LayoutManager.ComboItem) selected).getId());
                } else {
                    data.put(key, selected);
                }
            }
        }
        return data;
    }
}