package cn.yesand.intellijplugin.lweintellijplugin.converter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;

/**
 * Java PO对象转JSON转换器
 */
public class JavaPoToJsonConverter implements StructToJsonConverter {
    
    private static final Pattern CLASS_PATTERN = Pattern.compile("\\s*(?:public\\s+)?class\\s+\\w+");
    private static final Pattern FIELD_PATTERN = Pattern.compile("\\s*(?:private|public|protected)?\\s+(\\w+)\\s+(\\w+)(?:\\s*=\\s*[^;]+)?;");
    
    @Override
    public boolean canConvert(String text) {
        return text.contains("class") && CLASS_PATTERN.matcher(text).find();
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
            String type = matcher.group(1);
            String name = matcher.group(2);
            fields.add(new Field(name, type));
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
                case "integer":
                case "long":
                case "short":
                case "byte":
                case "float":
                case "double":
                    json.append("0");
                    break;
                case "boolean":
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