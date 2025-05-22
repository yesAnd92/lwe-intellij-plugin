package cn.yesand.intellijplugin.lweintellijplugin.converter;

/**
 * 结构体转JSON接口
 * 定义将不同语言的结构体转换为JSON的通用方法
 */
public interface StructToJsonConverter {
    /**
     * 检查给定的文本是否可以被此转换器处理
     * @param text 要检查的文本
     * @return 如果可以处理返回true，否则返回false
     */
    boolean canConvert(String text);
    
    /**
     * 将结构体文本转换为JSON结构
     * @param text 结构体文本
     * @return JSON结构字符串
     */
    String convertToJson(String text);
}