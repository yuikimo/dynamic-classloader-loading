package org.example.core;

/**
 * 实现该接口即可等待变色龙工作。实际你传入的 javaSrc 就是要实现该方法
 */
public interface Chameleon<T> {
    void process(T t);
}
