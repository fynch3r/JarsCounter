package com.sec.jc.handle;

import com.google.common.collect.UnmodifiableIterator;
import com.google.common.reflect.ClassPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collection;


/**
 * @program: JarsCounter
 * @description:
 * @author: 0range
 * @create: 2021-03-14 13:49
 **/


public class ClassResourceEnumerator {
    private static Logger logger = LoggerFactory.getLogger(ClassResourceEnumerator.class);

    private final ClassLoader classLoader;

    public ClassResourceEnumerator(ClassLoader classLoader) throws IOException {
        this.classLoader = classLoader;
    }

    public static interface ClassResource {
        public InputStream getInputStream() throws IOException;
        public String getName();
    }

    private static class PathClassResource implements ClassResource {
        private final Path path;

        private PathClassResource(Path path) {
            this.path = path;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return Files.newInputStream(path);
        }

        @Override
        public String getName() {
            return path.toString();
        }
    }

    private static class ClassLoaderClassResource implements ClassResource {
        private final ClassLoader classLoader;
        private final String resourceName;

        private ClassLoaderClassResource(ClassLoader classLoader, String resourceName) {
            this.classLoader = classLoader;
            this.resourceName = resourceName;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return classLoader.getResourceAsStream(resourceName);
        }

        @Override
        public String getName() {
            return resourceName;
        }
    }

    //获取运行时的类
    public Collection<ClassResource> getRuntimeClasses() throws IOException {
        URL stringClassUrl = Object.class.getResource("String.class");
        URLConnection connection = stringClassUrl.openConnection();
        Collection<ClassResource> result = new ArrayList<>();
        if (connection instanceof JarURLConnection) {
            URL runtimeUrl = ((JarURLConnection) connection).getJarFileURL();
            URLClassLoader classLoader = new URLClassLoader(new URL[]{runtimeUrl});

            for (ClassPath.ClassInfo classInfo : ClassPath.from(classLoader).getAllClasses()) {
                result.add(new ClassLoaderClassResource(classLoader, classInfo.getResourceName()));
            }

            logger.info("Java8 Runtime Classes Count : "+result.size());
            return result;
        }
        return result;
    }

//    //获取全部加载的类
//    public Collection<String> getTargetClassLoaderClasses() throws IOException {
//
//        //获取rt.jar之中的类
//        Collection<String> result = new ArrayList<>(getRuntimeClasses());
//
//        int count = 0;
//        for (ClassPath.ClassInfo classInfo : ClassPath.from(classLoader).getAllClasses()) {
//            // 如果classInfo是spring框架或者带tabby名字的类，直接跳过不分析
//            if(classInfo.getName().startsWith("org.springframework")) {
//                // 规避springframebook
//                continue;
//            }
//            count++;
//            result.add(classInfo.getName());
//        }
//
//        //输出第三方jar包的数量
//        logger.info("Jar Classes Count : "+ count);
//        logger.info("ALL Classes Count : "+ result.size());
//
//        return result;
//
//    }

    //获取获取全部类 runtime+target
    public Collection<ClassResource> getAllClasses() throws IOException {
        //先获取运行时jar包
        Collection<ClassResource> runtimeJarsStore = new ArrayList<>(getRuntimeClasses());

        //待分析jar包
        Collection<ClassResource> targetJarsStore = new ArrayList<>();

        //获取[待分析jar包]扫描迭代器,这个classLoader已经将待分析jar包加载
        UnmodifiableIterator it = ClassPath.from(this.classLoader).getAllClasses().iterator();


        while(it.hasNext()) {
            ClassPath.ClassInfo classInfo = (ClassPath.ClassInfo)it.next();
            if(!classInfo.getName().startsWith("org.springframework")){
                targetJarsStore.add(new ClassLoaderClassResource(classLoader, classInfo.getResourceName()));
            }
        }

        logger.info("Target Runtime Classes Count : "+ targetJarsStore.size());

        //总集合
        Collection<ClassResource> jarsStore = new ArrayList<>();

        jarsStore.addAll(runtimeJarsStore);

        jarsStore.addAll(targetJarsStore);

        //返回加载所有的jar的collection
        return jarsStore;
    }










}
