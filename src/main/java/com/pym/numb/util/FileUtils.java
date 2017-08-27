package com.pym.numb.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2017/8/24.
 */
public class FileUtils {
 /*   public static List<Class<?>> getClassFromClasspath(String preName) {
        List<File> list = getFiles(FileUtils.class.getResource("/").getPath()
                        + preName.replaceAll("\\.","/"),
                                         ".class");
        List<Class<?>> classes = new ArrayList<Class<?>>();
        list.forEach(file -> {
            String clp = file.getAbsolutePath().replaceAll("(/)|(\\\\)",".");
            clp = clp.substring(clp.indexOf(preName), clp.length() - 6);
            try {
                classes.add(Thread.currentThread().getContextClassLoader().loadClass(clp));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("找不到类"+clp, e);
            }
        });
        return classes;
    }
    public static List<Class<?>> getClassFromJar(String jarPath, String starWiths) {
        try {
            JarFile jarFile = new JarFile(jarPath);
            List<Class<?>> list = new ArrayList<Class<?>>();
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                String ename = jarEntry.getName().replaceAll("(/)|(\\\\)", ".");
                if (ename.startsWith(starWiths) && ename.endsWith(".class")) {
                    list.add(Thread.currentThread().getContextClassLoader().loadClass(ename.substring(0,ename.length()-6)));
                }
            }
            return list;
        } catch (Exception e) {
            throw new RuntimeException("获取类文件失败",e);
        }
    }*/
    public static List<File> getFiles(String dir, String endWiths) {
        List<File> list = new ArrayList<File> ();
        File file = new File(dir);
        if (!file.exists()) {
            return list;
        }
        if (file.isFile() && file.getName().endsWith(endWiths)) {
            list.add(file);
        } else {
            Arrays.asList(file.listFiles()).forEach(f -> {
                list.addAll(getFiles(f.getAbsolutePath(),endWiths));
            });
        }
        return list;
    }


}
