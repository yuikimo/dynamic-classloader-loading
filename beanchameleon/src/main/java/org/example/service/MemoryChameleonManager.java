package org.example.service;

import org.example.annotation.ChameleonX;
import org.example.core.Chameleon;
import org.example.core.ChameleonSrcCode;
import org.example.pipeline.ChainContext;
import org.example.pipeline.ChainProcess;

/**
 * 基于内存管理器的变色龙
 */
public class MemoryChameleonManager extends ChameleonManagerService implements ChainProcess<ChameleonSrcCode> {

    @Override
    public void process (ChainContext<ChameleonSrcCode> context) {
        // 获取上下文中的数据，再获取数据中 ChameleonX 的分组，注册到内存管理器中
        final ChameleonSrcCode processModel = context.getProcessModel();
        final Chameleon chameleon = processModel.getChameleon();
        final String name = chameleon.getClass().getAnnotation(ChameleonX.class).value();

        try {
            super.register(name, processModel.getJavaSrc(), chameleon);
        } catch (Exception e) {
            context.setNeedBreak(true);
            context.setException(e);
        }
    }
}
