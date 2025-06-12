package com.game.demo;

import javax.swing.*;
import javax.swing.table.*;
import java.util.*;

public class TableSearchHelper {
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> rowSorter;
    private String tableName = "";
    private Map<String, List<String>> tableSearchColumns = new HashMap<>();
    private TableDisplayHelper tableHelper;

    public TableSearchHelper(JTable table, TableRowSorter<DefaultTableModel> rowSorter, TableDisplayHelper tableHelper) {
        this.tableHelper = tableHelper;
        this.table = table;
        this.rowSorter = rowSorter;
        // Cấu hình cột tìm kiếm cho từng bảng
        tableSearchColumns.put("users", Arrays.asList("username", "email"));
        tableSearchColumns.put("characters", Arrays.asList("name", "className"));
        tableSearchColumns.put("item", Arrays.asList("name", "type"));
        tableSearchColumns.put("monster", Arrays.asList("name", "behavior"));
        tableSearchColumns.put("monsterdrop", Arrays.asList("monsterId", "itemId"));
        tableSearchColumns.put("map", Arrays.asList("name", "level"));
        tableSearchColumns.put("skill", Arrays.asList("name", "className"));
        tableSearchColumns.put("iteminstance", Arrays.asList("itemId"));
        tableSearchColumns.put("skillupdate", Arrays.asList("skillId"));
    }

    // Gọi hàm này khi đổi bảng
    public void setTableName(String tableName) {
        this.tableName = tableName.toLowerCase();
    }

    // Gọi hàm này khi searchField thay đổi
    public void filter(String text) {
    if (text.trim().isEmpty()) {
        rowSorter.setRowFilter(null);
    } else {
        String lower = text.toLowerCase();
        List<String> searchCols = tableSearchColumns.getOrDefault(tableName, null);
        if (searchCols != null) {
            rowSorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    DefaultTableModel model = entry.getModel();
                    for (String col : searchCols) {
                        int idx = model.findColumn(col);
                        if (idx >= 0) {
                            Object val = entry.getValue(idx);
                            String colLower = col.toLowerCase();
                            String displayVal = "";
                            // Sử dụng lookupName cho các cột id
                            if (colLower.equals("itemid")) {
                                displayVal = tableHelper.lookupName("item", val);
                            } else if (colLower.equals("monsterid")) {
                                displayVal = tableHelper.lookupName("monster", val);
                            } else if (colLower.equals("skillid")) {
                                displayVal = tableHelper.lookupName("skill", val);
                            } else if (colLower.equals("mapid")) {
                                displayVal = tableHelper.lookupName("map", val);
                            } else {
                                displayVal = val == null ? "" : val.toString();
                            }
                            // So sánh cả id và tên lookup
                            if ((val != null && val.toString().toLowerCase().contains(lower)) ||
                                (displayVal != null && displayVal.toLowerCase().contains(lower))) {
                                return true;
                            }
                        }
                    }
                    return false;
                }
            });
        } else {
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }
}
}