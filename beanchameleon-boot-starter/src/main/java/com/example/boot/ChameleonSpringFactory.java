package com.example.boot;

import org.example.annotation.ChameleonX;
import org.example.core.Chameleon;
import org.example.core.ChameleonSrcCode;
import org.example.pipeline.ChainContext;
import org.example.pipeline.ChainProcess;
import org.example.service.ChameleonManagerService;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

public class ChameleonSpringFactory extends ChameleonManagerService implements ChainProcess<ChameleonSrcCode> {

    private DefaultListableBeanFactory beanFactory;

    public ChameleonSpringFactory(DefaultListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public void process(ChainContext<ChameleonSrcCode> context) {
        final ChameleonSrcCode processModel = context.getProcessModel();
        final Chameleon chameleon = processModel.getChameleon();
        final String name = chameleon.getClass().getAnnotation(ChameleonX.class).value();

        try {
            super.register(name, processModel.getJavaSrc(), chameleon);
        } catch (BeanDefinitionStoreException e) {
            context.setNeedBreak(true);
            context.setException(e);
        }
    }

    @Override
    protected void rem(String chameleonName, String className) {
        beanFactory.destroySingleton(className);
    }

    @Override
    protected void save(String name, String javaSrc, Chameleon chameleon) {
        String beanName = chameleon.getClass().getName();
        if (beanFactory.containsBean(beanName)) {
            rem(null, beanName);
        }
        beanFactory.registerSingleton(beanName, chameleon);
        beanFactory.autowireBean(chameleon);
    }
}
