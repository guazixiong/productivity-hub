package common.core.domain;

/**
 * 字段变更信息类.
 *
 * @author: pangdi
 * @date: 2025-11-27 18:25:59
 * @version: 1.0
 */
public class FieldChangeInfo {
    private String name;    // 字段中文名称
    private Object value;   // 字段值
    private String field;   // 字段英文名称

    public FieldChangeInfo() {}

    public FieldChangeInfo(String name, Object value, String field) {
        this.name = name;
        this.value = value;
        this.field = field;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Object getValue() { return value; }
    public void setValue(Object value) { this.value = value; }
    
    public String getField() { return field; }
    public void setField(String field) { this.field = field; }
}