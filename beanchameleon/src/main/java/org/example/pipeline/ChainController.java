package org.example.pipeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 责任链控制器
 */
public class ChainController {
    public static final String DEFAULT = "default";

    private Map<String, Collection<ChainProcess>> processMap;

    public ChainController() {
        processMap = new ConcurrentHashMap<>();
    }

    public void addChain(String template, Collection<ChainProcess> chains) {
        if (!processMap.containsKey(template)) {
            processMap.put(template, new ArrayList<>());
        }
        processMap.get(template).addAll(chains);
    }

    public void addChain(String template, ChainProcess chain) {
        if (!processMap.containsKey(template)) {
            processMap.put(template, new ArrayList<>());
        }
        processMap.get(template).add(chain);
    }

    public ChainContext process(String template, ChainContext context) {
        for (ChainProcess businessProcess : processMap.get(template)) {
            businessProcess.process(context);
            if (context.getNeedBreak()) {
                break;
            }
        }
        return context;
    }

    public ChainContext process(ChainContext context) {
        return process(DEFAULT, context);
    }

}
