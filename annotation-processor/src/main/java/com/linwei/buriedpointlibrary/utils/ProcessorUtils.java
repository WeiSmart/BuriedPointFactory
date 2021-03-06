package com.linwei.buriedpointlibrary.utils;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * @Author: WS
 * @Time: 2020/4/30
 * @Description: 注解处理器工具类
 */
public class ProcessorUtils {

    private ProcessingEnvironment mProcessingEnv;
    private static ProcessorUtils mProcessorUtils = null;

    private static Set<String> supportTypes = new HashSet<>();

    private ProcessorUtils(ProcessingEnvironment env) {
        this.mProcessingEnv = env;
        initDefaultData();
    }

    public static ProcessorUtils getInstance(ProcessingEnvironment env) {
        if (mProcessorUtils == null) {
            synchronized (ProcessorUtils.class) {
                if (mProcessorUtils == null) {
                    mProcessorUtils = new ProcessorUtils(env);
                }
            }
        }
        return mProcessorUtils;
    }

    /**
     * Types工具类
     *
     * @return
     */
    public Types processorTypeUtils() {
        return mProcessingEnv.getTypeUtils();
    }

    /**
     * Element工具类
     *
     * @return
     */
    public Elements processorElementUtils() {
        return mProcessingEnv.getElementUtils();
    }

    /**
     * Filer工具
     *
     * @return
     */
    public Filer processorFiler() {
        return mProcessingEnv.getFiler();
    }

    /**
     * 日志对象
     *
     * @return
     */
    private Messager processorMessage() {
        return mProcessingEnv.getMessager();
    }

    /**
     * 错误日志
     *
     * @param msg
     * @param args
     */
    public void eLog(String msg, Object... args) {
        LogMessage(Diagnostic.Kind.ERROR, msg, args);
    }

    /**
     * 警告日志
     *
     * @param msg
     * @param args
     */
    public void wLog(String msg, Object... args) {
        LogMessage(Diagnostic.Kind.WARNING, msg, args);
    }

    /**
     * 强制警告日志
     *
     * @param msg
     * @param args
     */
    public void mwLog(String msg, Object... args) {
        LogMessage(Diagnostic.Kind.MANDATORY_WARNING, msg, args);
    }

    /**
     * 注意日志
     *
     * @param msg
     * @param args
     */
    public void nLog(String msg, Object... args) {
        LogMessage(Diagnostic.Kind.NOTE, msg, args);
    }


    /**
     * 日志输出
     *
     * @param kind
     * @param msg
     * @param args
     */
    private void LogMessage(Diagnostic.Kind kind, String msg, Object... args) {
        Messager messager = processorMessage();
        if (messager != null) {
            messager.printMessage(kind, String.format(msg, args));
        }
    }

    /**
     * 初始化默认数据
     */
    private void initDefaultData() {
        supportTypes.add(int.class.getSimpleName());
        supportTypes.add(int[].class.getSimpleName());
        supportTypes.add(short.class.getSimpleName());
        supportTypes.add(short[].class.getSimpleName());
        supportTypes.add(long.class.getSimpleName());
        supportTypes.add(long[].class.getSimpleName());
        supportTypes.add(byte.class.getSimpleName());
        supportTypes.add(byte[].class.getSimpleName());
        supportTypes.add(double.class.getSimpleName());
        supportTypes.add(double[].class.getSimpleName());
        supportTypes.add(float.class.getSimpleName());
        supportTypes.add(float[].class.getSimpleName());
        supportTypes.add(String.class.getSimpleName());
        supportTypes.add(String[].class.getSimpleName());
        supportTypes.add(boolean.class.getSimpleName());
        supportTypes.add(boolean[].class.getSimpleName());
        supportTypes.add(char.class.getSimpleName());
        supportTypes.add(char[].class.getSimpleName());
        supportTypes.add("Bundle");
    }

    /**
     * 根据{{@link Element}}信息，获取包名信息
     *
     * @param element
     * @return
     */
    public String getPackageName(Element element) {
        String clazzName = element.getSimpleName().toString();
        String clazzCompleteName = element.toString();
        return clazzCompleteName.substring(0,
                clazzCompleteName.length() - clazzName.length() - 1);
    }

    /**
     * 判断是否是String类型或者数组或者bundle，因为这三种类型getIntent()不需要默认值
     *
     * @param typeName
     * @return
     */
    public boolean isElementNoDefaultValue(String typeName) {
        return (String.class.getName().equals(typeName) || typeName.contains("[]") || typeName.contains("Bundle"));
    }

    /**
     * 获得注解要传递参数的类型
     *
     * @param typeName typeName 注解获取到的参数类型
     * @return
     */
    public String getIntentTypeName(String typeName) {
        for (String name : supportTypes) {
            if (name.equals(getSimpleName(typeName))) {
                return name.replaceFirst(String.valueOf(name.charAt(0)), String.valueOf(name.charAt(0)).toUpperCase())
                        .replace("[]", "Array");
            }
        }
        return "";
    }

    /**
     * 获取类的的名字的字符串
     *
     * @param typeName 可以是包名字符串，也可以是类名字符串
     * @return
     */
    private String getSimpleName(String typeName) {
        if (typeName.contains(".")) {
            return typeName.substring(typeName.lastIndexOf(".") + 1);
        } else {
            return typeName;
        }
    }

    /**
     * 判断字符串{{@code value}}是否为空
     *
     * @param value
     * @return
     */
    public boolean isNotEmpty(String value) {
        if (value != null && value.length() > 0) {
            return true;
        }
        return false;
    }


    /**
     * 根据配置信息，生成java文件
     *
     * @param className     要生成的类的名字
     * @param packageName   生成类所在的包的名字
     * @param methodSpec
     * @param processingEnv
     * @param listField
     */
    public void writeToFile(String className, String packageName, MethodSpec methodSpec,
                            ProcessingEnvironment processingEnv, ArrayList<FieldSpec> listField) {
        TypeSpec typeSpec;
        if (listField == null) {
            typeSpec = TypeSpec.classBuilder(className)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addMethod(methodSpec).build();
        } else {
            typeSpec = TypeSpec.classBuilder(className)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addMethod(methodSpec)
                    .addFields(listField)
                    .build();
        }

        JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();
        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
