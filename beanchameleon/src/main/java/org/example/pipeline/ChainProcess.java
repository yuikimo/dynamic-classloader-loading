package org.example.pipeline;

/**
 * <T extends ChainModel> 传输数据上下文，制定规范，必须实现ProcessModel
 * @param <T>
 */
public interface ChainProcess<T extends ChainModel> {

    /**
     * 真正处理逻辑
     * @param context
     */
    void process(ChainContext<T> context);
}

