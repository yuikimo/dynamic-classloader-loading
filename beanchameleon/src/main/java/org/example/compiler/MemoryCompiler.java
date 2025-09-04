package org.example.compiler;

import org.example.core.ChameleonSrcCode;
import org.example.pipeline.ChainContext;
import org.example.pipeline.ChainProcess;

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 编译源代码并保存至内存,可惜类无法主动卸载,不然 remove 的时候可以卸载，避免占用内存
 */
public class MemoryCompiler implements Compiler, ChainProcess<ChameleonSrcCode> {

    /**
     * 编译指定java源代码
     * @param javaSrc java源代码
     * @return 返回类的全限定名和编译后的class字节码字节数组的映射
     */
    @Override
    public Map<String, byte[]> compile(String javaSrc) throws IOException {
        // 提取类名并拼接为文件名(eg: Test.java)
        Pattern pattern = Pattern.compile("public\\s+class\\s+(\\w+)");
        Matcher matcher = pattern.matcher(javaSrc);
        if (matcher.find()) {
            return compile(matcher.group(1) + ".java", javaSrc);
        }
        return null;
    }

    /**
     * 编译指定java源代码
     * @param javaName java文件名
     * @param javaSrc  java源码内容
     * @return 返回类的全限定名和编译后的class字节码字节数组的映射
     */
    public Map<String, byte[]> compile(String javaName, String javaSrc) throws IOException {
        // 获取 java 编译器
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        // 创建标准文件管理器，用于处理编译文件操作
        StandardJavaFileManager stdManager = compiler.getStandardFileManager(null, null, null);
        // 使用内存管理 java 文件,这里就不用将载入的 java 源代码存入磁盘，而是基于内存了
        try (MemoryJavaFileManager manager = new MemoryJavaFileManager(stdManager)) {
            // 编译，将 Java 源码字符串包装成编译器可处理的虚拟文件
            JavaFileObject javaFileObject = manager.makeStringSource(javaName, javaSrc);
            // 创建编译任务
            JavaCompiler.CompilationTask task =
                    compiler.getTask(null, manager, null, null, null, Arrays.asList(javaFileObject));
            // 执行编译并处理结果
            if (task.call()) {
                return manager.getClassBytes();
            }
        }
        return null;
    }

    @Override
    public void process(ChainContext<ChameleonSrcCode> context) {
        try {
            final Map<String, byte[]> classMap = compile(context.getProcessModel().getJavaSrc());
            context.getProcessModel().setClassMap(classMap);
        } catch (IOException e) {
            context.setNeedBreak(true);
            context.setException(e);
        }
    }

    /**
     * 将生成的 .class 字节码存储到内存
     * 继承 ForwardingJavaFileManager（代理标准文件管理器）
     */
    final static class MemoryJavaFileManager extends ForwardingJavaFileManager {
        // Java源文件扩展名
        private final static String EXT = ".java";
        // 类名 -> 字节码字节数组的映射
        private Map<String, byte[]> classBytes;

        public MemoryJavaFileManager(JavaFileManager fileManager) {
            super(fileManager);
            classBytes = new HashMap<>();
        }

        public Map<String, byte[]> getClassBytes() {
            return classBytes;
        }

        public void close() {
            classBytes = new HashMap<>();
        }

        public void flush() {

        }

        /**
         * 将Java源码字符串包装成内存文件对象
         * 继承 SimpleJavaFileObject（表示内存中的文件）
         */
        private static class StringInputBuffer extends SimpleJavaFileObject {
            // 源码字符串
            final String code;

            // 调用父类构造器:
            //   toURI(name) - 为源文件生成虚拟URI
            //   Kind.SOURCE - 标识这是源码类型
            StringInputBuffer(String name, String code) {
                super(toURI(name), Kind.SOURCE);
                this.code = code;
            }

            // 获取源码内容（编译器调用）
            public CharBuffer getCharContent(boolean ignoreEncodingErrors) {
                // 将源码转为字符缓冲区
                return CharBuffer.wrap(code);
            }

            // 获取源码读取器
            public Reader openReader() {
                // 从字符串创建读取器
                return new StringReader(code);
            }
        }

        /**
         * 捕获编译器生成的字节码并存入 MemoryJavaFileManager.classBytes
         */
        private class ClassOutputBuffer extends SimpleJavaFileObject {
            // 类全限定名
            private String name;

            // 调用父类构造器:
            //   toURI(name) - 为类文件生成虚拟URI
            //   Kind.CLASS - 标识这是字节码类型
            ClassOutputBuffer(String name) {
                super(toURI(name), Kind.CLASS);
                this.name = name;
            }

            // 创建输出流（编译器调用）
            public OutputStream openOutputStream() {
                // 创建 ByteArrayOutputStream 缓存字节码
                return new FilterOutputStream(new ByteArrayOutputStream()) {
                    public void close() throws IOException {
                        out.close();
                        // 字节流关闭时，将字节码存入 classBytes 映射
                        ByteArrayOutputStream bos = (ByteArrayOutputStream) out;
                        classBytes.put(name, bos.toByteArray());
                    }
                };
            }
        }

        /**
         * 若输出类型为 .class，返回自定义的 ClassOutputBuffer（拦截字节码）
         * 其他类型委托给标准处理器
         */
        public JavaFileObject getJavaFileForOutput(JavaFileManager.Location location, String className,
                                                   JavaFileObject.Kind kind, FileObject sibling) throws IOException {
            // 只处理.class输出（拦截字节码生成）
            if (kind == JavaFileObject.Kind.CLASS) {
                // 返回字节码捕获器
                return new ClassOutputBuffer(className);
            } else {
                return super.getJavaFileForOutput(location, className, kind, sibling);
            }
        }

        // 在call() 方法内部被调用
        static JavaFileObject makeStringSource(String name, String code) {
            // 创建源码文件对象
            return new StringInputBuffer(name, code);
        }

        /**
         * 为内存中的源码/字节码生成唯一标识符（URI）
         * com.example.Main → URI mfm:///com/example/Main
         * @param name
         * @return
         */
        static URI toURI(String name) {
            // 生成虚拟 URI (格式: mfm:///包名/类名)
            File file = new File(name);

            // 如果文件存在，使用真实路径，不存在就创建内存专用URI
            if (file.exists()) {
                return file.toURI();
            } else {
                try {
                    final StringBuilder newUri = new StringBuilder();
                    newUri.append("mfm:///");
                    // 包路径转换：com.example.Main -> com/example/Main
                    newUri.append(name.replace('.', '/'));
                    // 保留.java扩展名
                    if (name.endsWith(EXT)) {
                        newUri.replace(newUri.length() - EXT.length(), newUri.length(), EXT);
                    }
                    return URI.create(newUri.toString());
                } catch (Exception exp) {
                    // 异常时返回默认URI
                    return URI.create("mfm:///com/sun/script/java/java_source");
                }
            }
        }

    }
}
