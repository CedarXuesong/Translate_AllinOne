package com.cedarxuesong.translate_allinone.gui.configui.support;

import com.cedarxuesong.translate_allinone.gui.configui.model.ParameterListLocation;
import com.cedarxuesong.translate_allinone.gui.configui.model.ParameterTreeRow;
import com.cedarxuesong.translate_allinone.utils.config.pojos.CustomParameterEntry;

import java.util.ArrayList;
import java.util.List;

public final class CustomParameterTreeSupport {
    private CustomParameterTreeSupport() {
    }

    public static int countEntries(List<CustomParameterEntry> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return 0;
        }
        int total = 0;
        for (CustomParameterEntry node : nodes) {
            if (node == null) {
                continue;
            }
            total++;
            total += countEntries(node.children);
        }
        return total;
    }

    public static List<ParameterTreeRow> listRows(List<CustomParameterEntry> root) {
        List<ParameterTreeRow> rows = new ArrayList<>();
        collectRows(root, "", 0, rows);
        return rows;
    }

    public static CustomParameterEntry findByPath(List<CustomParameterEntry> root, String path) {
        ParameterListLocation location = findLocation(root, path);
        if (location == null) {
            return null;
        }
        if (location.index() < 0 || location.index() >= location.list().size()) {
            return null;
        }
        return location.list().get(location.index());
    }

    public static ParameterListLocation findLocation(List<CustomParameterEntry> root, String path) {
        if (root == null || path == null || path.isBlank()) {
            return null;
        }
        List<Integer> indices = parsePathIndices(path);
        if (indices.isEmpty()) {
            return null;
        }

        List<CustomParameterEntry> currentList = root;
        for (int depth = 0; depth < indices.size() - 1; depth++) {
            int index = indices.get(depth);
            if (index < 0 || index >= currentList.size()) {
                return null;
            }
            CustomParameterEntry parent = currentList.get(index);
            if (parent == null) {
                return null;
            }
            if (parent.children == null) {
                parent.children = new ArrayList<>();
            }
            currentList = parent.children;
        }

        return new ParameterListLocation(currentList, indices.get(indices.size() - 1));
    }

    public static String composeChildPath(String parentPath, int index) {
        if (parentPath == null || parentPath.isBlank()) {
            return Integer.toString(index);
        }
        return parentPath + "." + index;
    }

    public static String parentPath(String path) {
        if (path == null || path.isBlank()) {
            return "";
        }
        int split = path.lastIndexOf('.');
        if (split < 0) {
            return "";
        }
        return path.substring(0, split);
    }

    public static CustomParameterEntry createDefaultEntry() {
        CustomParameterEntry entry = new CustomParameterEntry();
        entry.key = "param";
        entry.value = "";
        entry.is_object = false;
        entry.children = new ArrayList<>();
        return entry;
    }

    public static Object parseTypedValue(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.equalsIgnoreCase("true")) {
            return true;
        }
        if (trimmed.equalsIgnoreCase("false")) {
            return false;
        }
        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException ignored) {
        }
        try {
            return Double.parseDouble(trimmed);
        } catch (NumberFormatException ignored) {
        }
        return trimmed;
    }

    private static void collectRows(List<CustomParameterEntry> nodes, String parentPath, int depth, List<ParameterTreeRow> rows) {
        if (nodes == null) {
            return;
        }
        for (int i = 0; i < nodes.size(); i++) {
            CustomParameterEntry entry = nodes.get(i);
            if (entry == null) {
                continue;
            }
            String path = composeChildPath(parentPath, i);
            rows.add(new ParameterTreeRow(path, entry, depth));
            if (entry.is_object && entry.children != null && !entry.children.isEmpty()) {
                collectRows(entry.children, path, depth + 1, rows);
            }
        }
    }

    private static List<Integer> parsePathIndices(String path) {
        List<Integer> result = new ArrayList<>();
        String[] parts = path.split("\\.");
        for (String part : parts) {
            String trimmed = sanitize(part).trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            try {
                result.add(Integer.parseInt(trimmed));
            } catch (NumberFormatException ignored) {
                return new ArrayList<>();
            }
        }
        return result;
    }

    private static String sanitize(String value) {
        return value == null ? "" : value;
    }
}
