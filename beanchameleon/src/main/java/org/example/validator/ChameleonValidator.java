package org.example.validator;

import org.example.core.ChameleonSrcCode;
import org.example.pipeline.ChainContext;
import org.example.pipeline.ChainProcess;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 检查是否是一个合规的变色龙
 */
public class ChameleonValidator implements ChameleonValidatorService, ChainProcess<ChameleonSrcCode> {

    String patternAnnotation = "@ChameleonX\\(.*?\\)";

    String patternInterface = "implements\\s+([^\\{]+)";

    String patternMethod = "public void process\\([^)]*\\) \\{[^}]*\\}";

    String patternClass = "Chameleon(\\\\<.*?\\\\>)?";

    @Override
    public void process (ChainContext<ChameleonSrcCode> context) {
        try {
            validator(context.getProcessModel().getJavaSrc());
        } catch (JavaSrcValidatorException e) {
            context.setNeedBreak(true);
            context.setResponse(e.getMessage());
            context.setException(e);
        }
    }

    @Override
    public void validator (String javaSrc) throws JavaSrcValidatorException {
        validatorIsEmpty(javaSrc);
        validatorAnnotation(javaSrc);
        validatorInterface(javaSrc);
        validatorMethod(javaSrc);
    }

    // 检查是否为空
    protected void validatorIsEmpty(String javaSrc) throws JavaSrcValidatorException {
        if (javaSrc == null || javaSrc.isEmpty()){
            throw new JavaSrcValidatorException("Java src is empty");
        }
    }

    // 检查注解
    protected void validatorAnnotation(String javaSrc) throws JavaSrcValidatorException{
        Pattern regex = Pattern.compile(patternAnnotation);
        Matcher matcher = regex.matcher(javaSrc);
        if (!matcher.find()) {
            throw new JavaSrcValidatorException("Not found `@ChameleonX` annotation");
        }
    }

    // 检查实现接口
    protected void validatorInterface(String javaSrc) throws JavaSrcValidatorException{
        Pattern regex = Pattern.compile(patternInterface);
        Matcher matcher = regex.matcher(javaSrc);
        boolean flag = false;
        // 检测实现 Chameleon
        if (matcher.find()) {
            String interfaces = matcher.group(1).trim();
            String[] interfaceList = interfaces.split("\\s*,\\s*");
            String pattern = patternClass;
            for (String intf : interfaceList) {
                regex = Pattern.compile(pattern);
                matcher = regex.matcher(intf);
                if (matcher.find()){
                    return;
                }
            }
        }
        throw new JavaSrcValidatorException("Not found `Chameleon` interface");
    }

    // 检查方法 patternMethod
    protected void validatorMethod(String javaSrc) throws JavaSrcValidatorException{
        Pattern regex = Pattern.compile(patternMethod);
        Matcher matcher = regex.matcher(javaSrc);
        if (!matcher.find()){
            throw new JavaSrcValidatorException("Not found `public void process` method");
        }

    }
}
