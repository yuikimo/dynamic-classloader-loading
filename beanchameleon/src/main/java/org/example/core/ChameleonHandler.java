package org.example.core;

import org.example.service.ChameleonManagerService;

import java.util.Collection;

public class ChameleonHandler<T> implements ChameleonExec<T> {
    private final ChameleonManagerService chameleonManager;

    public ChameleonHandler(ChameleonManagerService chameleonManager) {
        this.chameleonManager = chameleonManager;
    }

    @Override
    public void exec (String name, T t) {
        final Collection<ChameleonBean> chameleons = chameleonManager.get(name);
        Do(chameleons, t);
    }

    private void Do(Collection<ChameleonBean> chameleons, T t) {
        if (chameleons != null && chameleons.size() > 0) {
            for (Chameleon chameleon : chameleons) {
                chameleon.process(t);
            }
        }
    }
}
