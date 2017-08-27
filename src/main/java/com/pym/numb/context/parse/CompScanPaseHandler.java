package com.pym.numb.context.parse;

import com.pym.numb.context.annotation.*;
import com.pym.numb.context.core.BeanMeta;
import com.pym.numb.context.core.NumbContainer;
import com.pym.numb.context.support.CompType;
import com.pym.numb.util.FileUtils;
import com.pym.numb.util.StrUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public class CompScanPaseHandler {

    public static List<Class<?>> getClassFromClasspath(String preName) {
        List<File> list = FileUtils.getFiles(FileUtils.class.getResource("/").getPath()
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
    }
    public static List<BeanMeta> handler(String preName, CompType[] compTypes) {
        String clsPath = NumbContainer.class.getResource("/").getPath();
        String libPath = clsPath.substring(0, clsPath.length() - 9);
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.addAll(getClassFromClasspath(preName));
        Arrays.asList(new File(libPath).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".jar");
            }
        })).forEach(jarfile -> {
            list.addAll(getClassFromJar(jarfile.getPath(), preName));
        });
        List<BeanMeta> beanMetas = new ArrayList<BeanMeta>();
        for (Class<?> cls : list) {
            Bean bean = cls.getAnnotation(Bean.class);
            Icp icp = cls.getAnnotation(Icp.class);
            ExceptionHandler eh = cls.getAnnotation(ExceptionHandler.class);
            ViewHandler viewHandler = cls.getAnnotation(ViewHandler.class);
            Controller controller = cls.getAnnotation(Controller.class);
            Service service = cls.getAnnotation(Service.class);
            String name = cls.getSimpleName();
            List<CompType> compTypeList = Arrays.asList(compTypes);
            if (bean != null) {
                if (!StrUtils.isEmpty(bean.value())) {
                    name = bean.value();
                }
                if (compTypeList.isEmpty() || compTypeList.contains(CompType.BEAN)) {
                    beanMetas.add(new BeanMeta(name,cls).compType(CompType.BEAN));
                }
            } else if (icp != null) {
                if (compTypeList.isEmpty() || compTypeList.contains(CompType.ICP)) {
                    beanMetas.add(new BeanMeta(name,cls).compType(CompType.ICP));
                }
            } else if (eh != null) {
                if (compTypeList.isEmpty() || compTypeList.contains(CompType.EXCEPTION_HANDLER)) {
                    beanMetas.add(new BeanMeta(name,cls).compType(CompType.EXCEPTION_HANDLER));
                }
            } else if (viewHandler != null) {
                if (compTypeList.isEmpty() || compTypeList.contains(CompType.VIEW_HANDLER)) {
                    beanMetas.add(new BeanMeta(name,cls).compType(CompType.VIEW_HANDLER));
                }
            } else if (controller != null) {
                if (!StrUtils.isEmpty(controller.value())) {
                    name = controller.value();
                }
                if (compTypeList.isEmpty() || compTypeList.contains(CompType.CONTROLLER)) {
                    beanMetas.add(new BeanMeta(name,cls).compType(CompType.CONTROLLER));
                }
            } else if (service != null) {
                if (!StrUtils.isEmpty(service.value())) {
                    name = service.value();
                }
                if (compTypeList.isEmpty() || compTypeList.contains(CompType.SERVICE)) {
                    beanMetas.add(new BeanMeta(name,cls).compType(CompType.SERVICE));
                }
            }
        }
        return beanMetas;
    }
}
