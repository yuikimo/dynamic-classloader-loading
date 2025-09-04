package org.example.core;

/**
 * 变色龙开始干活咯
 * @param <T>
 */
public interface ChameleonExec<T> {
    /**
     * @param name 指定的变色龙 {@link org.example.annotation.ChameleonX} # value
     * @param data 数据
     */
    void exec(String name, T data);
}
