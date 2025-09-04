package org.example.core;

/**
 * 存储在内存管理器中的数据对象
 */
public class ChameleonBean implements Chameleon{
    private final Chameleon chameleon;
    private final String javaSrc;
    private final String className;

    public ChameleonBean(Chameleon chameleon, String javaSrc, String className) {
        this.chameleon = chameleon;
        this.javaSrc = javaSrc;
        this.className = className;
    }

    @Override
    public void process (Object object) {
        chameleon.process(object);
    }

    @Override
    public String toString () {
        return "ChameleonBean{" +
               "chameleon=" + chameleon +
               ", javaSrc='" + javaSrc + '\'' +
               ", className='" + className + '\'' +
               '}';
    }

    public Chameleon getChameleon () {
        return chameleon;
    }

    public String getJavaSrc () {
        return javaSrc;
    }

    public String getClassName () {
        return className;
    }
}
