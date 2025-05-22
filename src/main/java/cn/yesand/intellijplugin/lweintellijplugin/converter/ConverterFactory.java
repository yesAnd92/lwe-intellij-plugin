package cn.yesand.intellijplugin.lweintellijplugin.converter;

import java.util.ArrayList;
import java.util.List;

/**
 * 转换器工厂类
 * 用于管理和获取所有可用的转换器
 */
public class ConverterFactory {
    
    private static final List<StructToJsonConverter> converters = new ArrayList<>();
    
    static {
        // 注册所有转换器
        converters.add(new JavaPoToJsonConverter());
        converters.add(new GoStructToJsonConverter());
        // 未来可以在这里添加更多语言的转换器
    }
    
    /**
     * 获取适合处理给定文本的转换器
     * @param text 要处理的文本
     * @return 合适的转换器，如果没有找到则返回null
     */
    public static StructToJsonConverter getConverter(String text) {
        for (StructToJsonConverter converter : converters) {
            if (converter.canConvert(text)) {
                return converter;
            }
        }
        return null;
    }
    
    /**
     * 将结构体文本转换为JSON
     * @param text 结构体文本
     * @return JSON字符串，如果没有合适的转换器则返回null
     */
    public static String convertToJson(String text) {
        StructToJsonConverter converter = getConverter(text);
        if (converter != null) {
            return converter.convertToJson(text);
        }
        return null;
    }
}