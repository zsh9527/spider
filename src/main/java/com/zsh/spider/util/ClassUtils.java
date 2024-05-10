package com.zsh.spider.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * ClassUtils
 *
 * @author zsh
 * @version 1.0.0
 * @date 2024/03/18 16:23
 */
public class ClassUtils {

    public static List<Class<?>> getAllSubclasses(Class<?> parentClass, String packageName) {
        List<Class<?>> subclasses = new ArrayList<>();
        // 遍历所有包
        List<Class<?>> classes = getClasses(packageName);

        // 检查每个类是否是指定父类的子类
        for (Class<?> cls : classes) {
            if (parentClass.isAssignableFrom(cls) && !cls.equals(parentClass)) {
                subclasses.add(cls);
            }
        }
        return subclasses;
    }

    public static List<Class<?>> getClasses(String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String path = packageName.replace('.', '/');
            Enumeration<URL> resources = classLoader.getResources(path);
            List<File> dirs = new ArrayList<>();

            // 遍历所有资源（文件夹或jar包）
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                dirs.add(new File(resource.getFile()));
            }

            // 遍历所有资源的文件夹
            for (File directory : dirs) {
                classes.addAll(findClasses(directory, packageName));
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return classes;
    }

    public static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        if (files == null) {
            return classes;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                classes.add(Class.forName(className));
            }
        }
        return classes;
    }
}
