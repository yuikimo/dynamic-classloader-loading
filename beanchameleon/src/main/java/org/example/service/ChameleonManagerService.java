package org.example.service;

import org.example.core.Chameleon;
import org.example.core.ChameleonBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * 管理变色龙
 */
public abstract class ChameleonManagerService {

    // 该Map有序，按名称自动排序
    private ConcurrentSkipListMap<String, Collection<ChameleonBean>> chameleons = new ConcurrentSkipListMap<>();

    public ConcurrentSkipListMap<String, Collection<ChameleonBean>> list() {
        return chameleons;
    }

    public Collection<ChameleonBean> get(String name) {
        return chameleons.get(name);
    }

    public void remove(String className) {
        remove(null, className);
    }

    /**
     * map.compute 更新或删除键值对
     * 1. 获取当前值
     * V oldValue = get(key);
     * </p>
     * 2. 调用BiFunction计算新值
     * V newValue = remappingFunction.apply(key, oldValue);
     * </p>
     * 3. 根据newValue判断更新 | 删除
     * 如果newValue==null则剔除元素。否则替换oldValue并返回newValue
     * @param chameleonName
     * @param className
     */
    public void remove(String chameleonName, String className) {
        // 提取共享逻辑到一个单独的方法
        BiFunction<String, Collection<ChameleonBean>, Collection<ChameleonBean>> processEntry = (key, value) -> {
            Collection<ChameleonBean> updatedList = value
                    .stream()
                    .filter(bean -> !bean.getClassName().equals(className))
                    .collect(Collectors.toList());
            return updatedList.isEmpty() ? null : updatedList;
        };

        // 如果chameleonName存在，只处理这一个条目
        if (chameleonName != null && !chameleonName.isEmpty() && chameleons.containsKey(chameleonName)) {
            chameleons.compute(chameleonName, processEntry);
        } else {
            // 如果chameleonName不存在，处理所有条目
            chameleons.keySet().forEach(key -> chameleons.compute(key, processEntry));
        }
        rem(chameleonName, className);
    }

    public void register(String name, String javaSrc, Chameleon chameleon) {
        if (!chameleons.containsKey(name)) {
            chameleons.put(name, new ArrayList<>());
        }

        final String className = chameleon.getClass().getName();
        final ChameleonBean chameleonBean = new ChameleonBean(chameleon, javaSrc, className);
        final ArrayList<ChameleonBean> chameleonBeans = (ArrayList<ChameleonBean>) chameleons.get(name);

        boolean isUpdating = false;
        for (int i = 0; i < chameleonBeans.size(); i++) {
            if (chameleonBeans.get(i).getClassName().equals(className)) {
                chameleonBeans.set(i, chameleonBean);
                isUpdating = true;
                break;
            }
        }

        if (!isUpdating) {
            chameleonBeans.add(chameleonBean);
        }

        save(name, javaSrc, chameleon);
    }

    /**
     * 预留的删除机制
     * @param chameleonName
     * @param className
     */
    protected void rem(String chameleonName, String className) {
    }

    /**
     * 预留的注册机制
     * @param clazz
     */
    public void register(Class clazz) {
    }

    /**
     * 预留的保存机制
     * @param name
     * @param javaSrc
     * @param chameleon
     */
    protected void save(String name, String javaSrc, Chameleon chameleon) {
    }

}
