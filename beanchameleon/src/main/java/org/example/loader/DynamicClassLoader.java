package org.example.loader;

import org.example.core.Chameleon;
import org.example.core.ChameleonSrcCode;
import org.example.pipeline.ChainContext;
import org.example.pipeline.ChainProcess;

import java.util.Map;

/**
 * 通过类加载器，在运行时加载字节码
 */
public class DynamicClassLoader extends ClassLoader implements ChainProcess<ChameleonSrcCode> {

    /**
     * 初始化类加载器，设置父加载器为当前类的类加载器（通常是系统类加载器）
     */
    public DynamicClassLoader() {
        super(DynamicClassLoader.class.getClassLoader());
    }

    /**
     * 编译字节码并返回 Class 对象
     * @param bytecode
     * @return
     * @throws ClassNotFoundException
     */
    public Class<?> load(Map<String, byte[]> bytecode) throws ClassNotFoundException {
        if (bytecode != null && !bytecode.isEmpty()) {
            // 获取文件名对应的字节码
            final String name = bytecode.keySet().iterator().next();
            byte[] buf = bytecode.get(name);
            // 若字节码为空，尝试从父加载器加载
            if (buf == null) {
                return super.findClass(name);
            }
            return new DynamicClassLoader().loadClassByte(buf);
        } else {
            throw new ClassNotFoundException("Can't found class");
        }
    }

    // 将字节码数组转换成 Class 对象
    public Class loadClassByte(byte[] classBytes) {
        return defineClass(null, classBytes, 0, classBytes.length);
    }

    @Override
    public void process(ChainContext<ChameleonSrcCode> context) {
        try {
            // 调用 load() 方法生成 Class 对象。
            final Class<?> load = load(context.getProcessModel().getClassMap());
            // 通过 load.newInstance() 创建实例
            context.getProcessModel().setChameleon((Chameleon) load.newInstance());
        } catch (Exception e) {
            context.setNeedBreak(true);
            context.setException(e);
        }
    }
}
