package org.example.pipeline;

import java.util.Collection;
import java.util.Map;

/**
 * 默认模版
 */
public class ChainTemplate {
    public static final String DEFAULT = "default";

    private Map<String, Collection<ChainProcess>> templates;

    public ChainTemplate(Map<String, Collection<ChainProcess>> templates) {
        this.templates = templates;
    }

    public Map<String, Collection<ChainProcess>> getTemplates() {
        return templates;
    }
}
