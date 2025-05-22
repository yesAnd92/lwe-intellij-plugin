package cn.yesand.intellijplugin.lweintellijplugin.converter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;

/**
 * Go结构体转JSON转换器
 */
public class GoStructToJsonConverter implements StructToJsonConverter {
    
    private static final Pattern STRUCT_PATTERN = Pattern.compile("\\s*type\\s+\\w+\\s+struct\\s*\\{");
    private static final Pattern FIELD_PATTERN = Pattern.compile("\\s*(\\w+)\\s+(\\w+)(?:\\s+`json:\"(\\w+)\"(?:[^`]*)`)?");
    
    @Override
    public boolean canConvert(String text) {
        return text.contains("struct") && STRUCT_PATTERN.matcher(text).find();
    }
    
    @Override
    public String convertToJson(String text) {
        List<Field> fields = extractFields(text);
        return generateJson(fields);
    }
    
    private List<Field> extractFields(String text) {
        List<Field> fields = new ArrayList<>();
        Matcher matcher = FIELD_PATTERN.matcher(text);
        
        while (matcher.find()) {
            String name = matcher.group(1);
            String type = matcher.group(2);
            String jsonName = matcher.group(3);
            
            // 如果有json标签，使用标签中的名称
            if (jsonName != null && !jsonName.isEmpty()) {
                fields.add(new Field(jsonName, type));
            } else {
                fields.add(new Field(name, type));
            }
        }
        
        return fields;
    }
    
    private String generateJson(List<Field> fields) {
        StringBuilder json = new StringBuilder("{\n");
        
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            json.append("  \"").append(field.name).append("\": ");
            
            switch (field.type.toLowerCase()) {
                case "int":
                case "int8":
                case "int16":
                case "int32":
                case "int64":
                case "uint":
                case "uint8":
                case "uint16":
                case "uint32":
                case "uint64":
                case "float32":
                case "float64":
                    json.append("0");
                    break;
                case "bool":
                    json.append("false");
                    break;
                case "string":
                    json.append("\"\"");
                    break;
                default:
                    json.append("null");
                    break;
            }
            
            if (i < fields.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        
        json.append("}");
        return json.toString();
    }
    
    private static class Field {
        String name;
        String type;
        
        Field(String name, String type) {
            this.name = name;
            this.type = type;
        }
    }
}