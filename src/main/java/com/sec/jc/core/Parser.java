package com.sec.jc.core;

import com.sec.jc.handle.ClassResourceEnumerator;
import com.sec.jc.util.FileUtils;
import com.sec.jc.util.JarsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @program: JarsCounter
 * @description:
 * @author: 0range
 * @create: 2021-03-14 13:43
 **/


public class Parser {

    private static Logger logger = LoggerFactory.getLogger(Parser.class);

    public static String EXPLODED_JARS_PATH = String.join(File.separator, System.getProperty("user.dir"), "exploded-jars");

    public static void analysis(Path path,boolean dirflag) throws IOException, URISyntaxException {

        if(Files.exists(Paths.get(EXPLODED_JARS_PATH))){
            //删除缓存
            JarsUtils.deleteDirectory(Paths.get(EXPLODED_JARS_PATH));
        }
        //获取第三方jar包路径
        List<Path> targets = FileUtils.getTargetDirectoryJarFiles(path);

        //新建临时根目录 ../temp-dirs/
        final Path tmpRootDir = Files.createDirectory(Paths.get(EXPLODED_JARS_PATH));

        //获取classpath的urls 稍后用于类加载
        final List<Path> jarPathUrls = new ArrayList<>();

        //反编译jar包为根目录下的临时文件夹
        for (Iterator<Path> it = targets.iterator(); it.hasNext(); ) {
            Path jarpath = it.next();
            //将jars解压到临时文件夹
            JarsUtils.Jar2TempDir(jarpath,tmpRootDir);
            //加载到类加载器待加载列表
            jarPathUrls.add(jarpath);
        }

        //如果不保存临时文件
        if(!dirflag){
            //程序关闭时删除临时文件夹
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    JarsUtils.deleteDirectory(tmpRootDir);
                } catch (IOException e) {
                    logger.error("Error cleaning up temp directory " + tmpRootDir.toString(), e);
                }
            }));
        }

        //初始化jar包，获得一个类加载器，该类加载器已经将[待分析jar包]加载
        ClassLoader classLoader = JarsUtils.getJarClassLoader(jarPathUrls);

        //包装为一个类枚举器对象
        final ClassResourceEnumerator classResourceEnumerator = new ClassResourceEnumerator(classLoader);

        //将全部类（runtime+target-jars）一起获取 封装 在枚举类加载器里面
        Collection<ClassResourceEnumerator.ClassResource> allClasses = classResourceEnumerator.getAllClasses();

        //到这里已经获取了全部的待分析类 存放在allClasses里面 格式为 ClassResourceEnumerator.ClassResource
        logger.info("ALL Classes Count "+ allClasses.size());

    }
}
