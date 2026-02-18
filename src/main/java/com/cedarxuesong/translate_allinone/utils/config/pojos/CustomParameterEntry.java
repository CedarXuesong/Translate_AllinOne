package com.cedarxuesong.translate_allinone.utils.config.pojos;

import java.util.ArrayList;
import java.util.List;

public class CustomParameterEntry {
    public String key = "parameter_name";

    public String value = "parameter_value";

    public boolean is_object = false;

    public List<CustomParameterEntry> children = new ArrayList<>();

    public CustomParameterEntry deepCopy() {
        CustomParameterEntry copy = new CustomParameterEntry();
        copy.key = this.key;
        copy.value = this.value;
        copy.is_object = this.is_object;
        copy.children = deepCopyList(this.children);
        return copy;
    }

    public static List<CustomParameterEntry> deepCopyList(List<CustomParameterEntry> source) {
        List<CustomParameterEntry> result = new ArrayList<>();
        if (source == null) {
            return result;
        }
        for (CustomParameterEntry entry : source) {
            if (entry == null) {
                continue;
            }
            result.add(entry.deepCopy());
        }
        return result;
    }
}
