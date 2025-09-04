package org.example.core;

import org.example.pipeline.ChainModel;

import java.util.Map;

/**
 * 责任链上下文中的数据
 * @param <T>
 */
public class ChameleonSrcCode<T> implements ChainModel {
    private final String javaSrc;
    // 类的实例对象
    private Chameleon chameleon;
    // key 类名 value class字节码
    private Map<String, byte[]> classMap;

    public ChameleonSrcCode(String javaSrc) {
        this.javaSrc = javaSrc;
    }

    public String getJavaSrc() {
        return javaSrc;
    }

    public Chameleon getChameleon() {
        return chameleon;
    }

    public void setChameleon(Chameleon chameleon) {
        this.chameleon = chameleon;
    }

    public Map<String, byte[]> getClassMap() {
        return classMap;
    }

    public void setClassMap(Map<String, byte[]> classMap) {
        this.classMap = classMap;
    }
}
