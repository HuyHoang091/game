package com.game.demo;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TableDisplayHelper {
    public final Map<Object, String> itemNameCache;
    public final Map<Object, String> itemIconCache;
    public final Map<Object, String> monsterNameCache;
    public final Map<Object, String> mapNameCache;
    public final Map<Object, String> skillNameCache;

    java.util.Map<String, String> displayNames;

    public TableDisplayHelper(
            Map<Object, String> itemNameCache,
            Map<Object, String> itemIconCache,
            Map<Object, String> monsterNameCache,
            Map<Object, String> mapNameCache,
            Map<Object, String> skillNameCache) {
        this.itemNameCache = itemNameCache;
        this.itemIconCache = itemIconCache;
        this.monsterNameCache = monsterNameCache;
        this.mapNameCache = mapNameCache;
        this.skillNameCache = skillNameCache;
    }

    public void loadAllCaches() {
        loadCache("item", itemNameCache, itemIconCache);
        loadCache("monster", monsterNameCache, null);
        loadCache("map", mapNameCache, null);
        loadCache("skill", skillNameCache, null);
    }

    public void loadCache(String type, Map<Object, String> nameCache, Map<Object, String> iconCache) {
        try {
            String url = "http://localhost:8080/api/" + type + "/";
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                java.util.List<java.util.Map<String, Object>> list = mapper.readValue(response.body(),
                        java.util.List.class);
                for (var obj : list) {
                    Object id = obj.get("id");
                    Object name = obj.get("name");
                    if (id != null && name != null)
                        nameCache.put(id, name.toString());
                    if (iconCache != null && id != null && obj.get("icon") != null)
                        iconCache.put(id, obj.get("icon").toString());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String lookupName(String type, Object id) {
        if (id == null)
            return "";
        switch (type) {
            case "item":
                return itemNameCache.getOrDefault(id, id.toString());
            case "monster":
                return monsterNameCache.getOrDefault(id, id.toString());
            case "map":
                return mapNameCache.getOrDefault(id, id.toString());
            case "skill":
                return skillNameCache.getOrDefault(id, id.toString());
            default:
                return id.toString();
        }
    }

    public String getItemIconById(Object itemId) {
        if (itemId == null)
            return "";
        return itemIconCache.getOrDefault(itemId, "assets/Item/default.png");
    }

    // --- Renderer hiển thị ảnh từ resources ---
    public static class ImageRenderer extends DefaultTableCellRenderer {
        private int w, h;
        private final java.util.Map<String, ImageIcon> iconCache = new java.util.HashMap<>();

        public ImageRenderer(int w, int h) {
            this.w = w;
            this.h = h;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            if (value instanceof String && ((String) value).endsWith(".png")) {
                String path = value.toString();
                ImageIcon icon = iconCache.get(path);
                if (icon == null) {
                    java.net.URL imgUrl = getClass().getClassLoader().getResource(path);
                    if (imgUrl != null) {
                        ImageIcon rawIcon = new ImageIcon(imgUrl);
                        Image img = rawIcon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
                        icon = new ImageIcon(img);
                        iconCache.put(path, icon);
                    }
                }
                if (icon != null) {
                    return new JLabel(icon);
                }
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }

    public class EnemyReviewRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                    int row, int column) {
            String name = monsterNameCache.get(value);
            if (name != null) {
                String path = "assets/Enemy/" + name + "/review.png";
                java.net.URL imgUrl = getClass().getClassLoader().getResource(path);
                if (imgUrl != null) {
                    ImageIcon icon = new ImageIcon(imgUrl);
                    Image img = icon.getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH);
                    return new JLabel(new ImageIcon(img));
                }
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }

    // --- Renderer hiển thị tên từ id ---
    public class NameLookupRenderer extends DefaultTableCellRenderer {
        private String type;

        public NameLookupRenderer(String type) {
            this.type = type;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            String name = lookupName(type, value);
            return super.getTableCellRendererComponent(table, name, isSelected, hasFocus, row, column);
        }
    }

    // --- Hàm customizeTableDisplay ---
    public void customizeTableDisplay(JTable table, String tableName) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        // Monster: thêm cột review (ảnh), mapId hiển thị name(map)
        if (tableName.equalsIgnoreCase("monster")) {
            if (model.findColumn("review") == -1) {
                model.addColumn("review");
                for (int i = 0; i < model.getRowCount(); i++) {
                    String name = String.valueOf(model.getValueAt(i, model.findColumn("name")));
                    String path = "assets/Enemy/" + name + "/review.png";
                    model.setValueAt(path, i, model.getColumnCount() - 1);
                }
            }
            table.getColumn("review").setCellRenderer(new ImageRenderer(48, 48));
            if (model.findColumn("mapId") != -1) {
                table.getColumn("mapId").setCellRenderer(new NameLookupRenderer("map"));
            }
        }

        // MonsterDrop: monsterId, itemId hiển thị name, thêm review (icon item)
        if (tableName.equalsIgnoreCase("monsterdrop")) {
            if (model.findColumn("review") == -1) {
                model.addColumn("review");
                for (int i = 0; i < model.getRowCount(); i++) {
                    Object itemId = model.getValueAt(i, model.findColumn("itemId"));
                    String iconPath = getItemIconById(itemId);
                    model.setValueAt(iconPath, i, model.getColumnCount() - 1);
                }
            }
            table.getColumn("review").setCellRenderer(new ImageRenderer(32, 32));
            if (model.findColumn("monsterId") != -1) {
                table.getColumn("monsterId").setCellRenderer(new NameLookupRenderer("monster"));
            }
            if (model.findColumn("itemId") != -1) {
                table.getColumn("itemId").setCellRenderer(new NameLookupRenderer("item"));
            }
        }

        // Map: background, collisionlayer, preview là ảnh; enemyId, bossId hiển thị name(monster)
        if (tableName.equalsIgnoreCase("map")) {
            String[] imgCols = { "background", "collisionlayer", "preview" };
            for (String col : imgCols) {
                if (model.findColumn(col) != -1) {
                    table.getColumn(col).setCellRenderer(new ImageRenderer(64, 36));
                }
            }
            if (model.findColumn("enemyId") != -1) {
                table.getColumn("enemyId").setCellRenderer(new EnemyReviewRenderer());
            }
            if (model.findColumn("bossId") != -1) {
                table.getColumn("bossId").setCellRenderer(new EnemyReviewRenderer());
            }
        }

        // Item: icon là ảnh
        if (tableName.equalsIgnoreCase("item")) {
            if (model.findColumn("icon") != -1) {
                table.getColumn("icon").setCellRenderer(new ImageRenderer(32, 32));
            }
        }

        // ItemInstance: itemId hiển thị name(item)
        if (tableName.equalsIgnoreCase("iteminstance")) {
            if (model.findColumn("itemId") != -1) {
                table.getColumn("itemId").setCellRenderer(new NameLookupRenderer("item"));
            }
        }

        // Skill: icon là ảnh
        if (tableName.equalsIgnoreCase("skill")) {
            if (model.findColumn("icon") != -1) {
                table.getColumn("icon").setCellRenderer(new ImageRenderer(32, 32));
            }
        }

        // SkillUpdate: skillId hiển thị name(skill)
        if (tableName.equalsIgnoreCase("skillupdate")) {
            if (model.findColumn("skillId") != -1) {
                table.getColumn("skillId").setCellRenderer(new NameLookupRenderer("skill"));
            }
        }
    }

    public void setColumnDisplayNames(JTable table, String tableName) {
        displayNames = new java.util.HashMap<>();
        // Đặt tên hiển thị cho từng bảng
        if (tableName.equalsIgnoreCase("users")) {
            displayNames.put("id", "Mã người dùng");
            displayNames.put("username", "Tên đăng nhập");
            displayNames.put("password", "Mật khẩu");
            displayNames.put("email", "Email");
            displayNames.put("tiendo", "Tiến độ");
            displayNames.put("trangthai", "Trạng thái");
        } else if (tableName.equalsIgnoreCase("characters")) {
            displayNames.put("id", "Mã nhân vật");
            displayNames.put("userId", "Mã người dùng");
            displayNames.put("name", "Tên nhân vật");
            displayNames.put("level", "Cấp độ");
            displayNames.put("skillPoint", "Điểm kỹ năng");
            displayNames.put("gold", "Vàng");
            displayNames.put("exp", "Kinh nghiệm");
            displayNames.put("className", "Lớp nhân vật");
        } else if (tableName.equalsIgnoreCase("character_skills")) {
            displayNames.put("id", "Mã kỹ năng nhân vật");
            displayNames.put("characterId", "Mã nhân vật");
            displayNames.put("skillId", "Kỹ năng");
            displayNames.put("level", "Cấp độ");
            displayNames.put("slot", "Ô kỹ năng");
        } else if (tableName.equalsIgnoreCase("inventory")) {
            displayNames.put("id", "Mã kho đồ");
            displayNames.put("characterId", "Mã nhân vật");
            displayNames.put("itemId", "Mã vật phẩm");
            displayNames.put("itemInstanceId", "Mã vật phẩm cá nhân");
            displayNames.put("quantity", "Số lượng");
            displayNames.put("equipped", "Đã trang bị");
        } else if (tableName.equalsIgnoreCase("map")) {
            displayNames.put("id", "Mã bản đồ");
            displayNames.put("name", "Tên bản đồ");
            displayNames.put("level", "Cấp độ");
            displayNames.put("background", "Ảnh nền");
            displayNames.put("collisionlayer", "Lớp va chạm");
            displayNames.put("preview", "Ảnh xem trước");
            displayNames.put("enemyId", "Quái thường");
            displayNames.put("bossId", "Quái boss");
        } else if (tableName.equalsIgnoreCase("monsterdrop")) {
            displayNames.put("id", "Mã rơi quái");
            displayNames.put("monsterId", "Tên quái");
            displayNames.put("itemId", "Tên vật phẩm");
            displayNames.put("dropRate", "Tỷ lệ rơi");
        } else if (tableName.equalsIgnoreCase("skill")) {
            displayNames.put("id", "Mã kỹ năng");
            displayNames.put("name", "Tên kỹ năng");
            displayNames.put("className", "Lớp kỹ năng");
            displayNames.put("levelRequired", "Cấp độ yêu cầu");
            displayNames.put("maxLevel", "Cấp độ tối đa");
            displayNames.put("manaCost", "Mana tiêu hao");
            displayNames.put("mota", "Mô tả");
            displayNames.put("damage", "Sát thương(%)");
            displayNames.put("cooldown", "Thời gian hồi chiêu");
            displayNames.put("cauhinh", "Cấu hình");
            displayNames.put("icon", "Biểu tượng");
        } else if (tableName.equalsIgnoreCase("skillupdate")) {
            displayNames.put("id", "Mã cập nhật kỹ năng");
            displayNames.put("skillId", "Kỹ năng");
            displayNames.put("level", "Cấp độ");
            displayNames.put("levelRequired", "Cấp độ yêu cầu");
            displayNames.put("pointRequired", "Điểm yêu cầu");
            displayNames.put("goldRequired", "Vàng yêu cầu");
        } else if (tableName.equalsIgnoreCase("iteminstance")) {
            displayNames.put("id", "Mã vật phẩm cá nhân");
            displayNames.put("itemId", "Vật phẩm");
            displayNames.put("atk", "Tấn công");
            displayNames.put("def", "Phòng thủ");
            displayNames.put("hp", "Máu");
            displayNames.put("mp", "Mana");
            displayNames.put("critRate", "Tỷ lệ chí mạng");
            displayNames.put("critDmg", "Sát thương chí mạng");
        } else if (tableName.equalsIgnoreCase("item")) {
            displayNames.put("id", "Mã vật phẩm");
            displayNames.put("name", "Tên vật phẩm");
            displayNames.put("type", "Loại");
            displayNames.put("mota", "Mô tả");
            displayNames.put("thuoctinh", "Thuộc tính");
            displayNames.put("icon", "Biểu tượng");
        } else if (tableName.equalsIgnoreCase("monster")) {
            displayNames.put("id", "Mã quái");
            displayNames.put("name", "Tên quái");
            displayNames.put("level", "Cấp độ");
            displayNames.put("hp", "Máu");
            displayNames.put("cauhinh", "Cấu hình");
            displayNames.put("expReward", "Phần thưởng kinh nghiệm");
            displayNames.put("behavior", "Hành vi");
        }

        // Đổi tên header, không ảnh hưởng dữ liệu
        for (int i = 0; i < table.getColumnCount(); i++) {
            String col = table.getColumnName(i);
            if (displayNames.containsKey(col)) {
                table.getColumnModel().getColumn(i).setHeaderValue(displayNames.get(col));
            }
        }
        // Cập nhật lại header
        table.getTableHeader().repaint();
    }

    public Map<String, String> getDisplayNames() {
        return this.displayNames;
    }
}