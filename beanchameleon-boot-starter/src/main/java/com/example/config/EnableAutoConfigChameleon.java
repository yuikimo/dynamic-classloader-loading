package com.example.config;

import com.example.boot.ChameleonSpringFactory;
import org.example.compiler.MemoryCompiler;
import org.example.core.ChameleonHandler;
import org.example.loader.DynamicClassLoader;
import org.example.pipeline.ChainController;
import org.example.pipeline.ChainProcess;
import org.example.pipeline.ChainTemplate;
import org.example.service.MemoryChameleonManager;
import org.example.validator.ChameleonValidator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;

@Configuration
public class EnableAutoConfigChameleon {

    @Bean
    public MemoryChameleonManager memoryChameleonManager() {
        return new MemoryChameleonManager();
    }

    @Bean
    @ConditionalOnMissingBean(ChameleonHandler.class)
    public ChameleonHandler chameleonActionHandler(ChameleonSpringFactory managerService) {
        return new ChameleonHandler(managerService);
    }

    @Bean
    public ChameleonValidator ChameleonValidator() {
        return new ChameleonValidator();
    }

    @Bean
    public MemoryCompiler memoryCompiler() {
        return new MemoryCompiler();
    }

    @Bean
    public DynamicClassLoader dynamicClassLoader() {
        return new DynamicClassLoader();
    }

    @Bean
    public ChameleonSpringFactory chameleonSpringFactory(DefaultListableBeanFactory defaultListableBeanFactory) {
        return new ChameleonSpringFactory(defaultListableBeanFactory);
    }

    @Bean
    @ConditionalOnMissingBean(ChainTemplate.class)
    public ChainTemplate chainTemplate(ChameleonValidator chameleonValidator,
                                       MemoryCompiler memoryCompiler,
                                       DynamicClassLoader dynamicClassLoader,
                                       ChameleonSpringFactory chameleonSpringFactory) {
        final List<ChainProcess> list = Arrays.asList(chameleonValidator, memoryCompiler, dynamicClassLoader, dynamicClassLoader, chameleonSpringFactory);
        final Map<String, Collection<ChainProcess>> chameleons = new HashMap<>();
        chameleons.put(ChainTemplate.DEFAULT, list);
        return new ChainTemplate(chameleons);
    }

    @Bean
    @ConditionalOnMissingBean(ChainController.class)
    public ChainController chainController(ChainTemplate chainTemplate) {
        ChainController chainController = new ChainController();
        chainTemplate.getTemplates().forEach((k, v) -> chainController.addChain(k, v));
        return chainController;
    }

}
