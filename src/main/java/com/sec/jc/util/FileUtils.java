package com.sec.jc.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: JarsCounter
 * @description:
 * @author: 0range
 * @create: 2021-03-14 13:44
 **/


public class FileUtils {
    private static Logger logger = LoggerFactory.getLogger(FileUtils.class);


    public static List<Path> getTargetDirectoryJarFiles(Path target) throws IOException {
        List<Path> paths = new ArrayList<>();
        Path path = target.toAbsolutePath();
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Invalid jar path: " + path);
        }
        if(path.toFile().isFile()){
            paths.add(Paths.get(path.toAbsolutePath().toString()));
        }else{
            Files.walkFileTree(path, new SimpleFileVisitor<Path>(){
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if(file.getFileName().toString().endsWith(".jar")){
                        paths.add(Paths.get(file.toAbsolutePath().toString()));
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }

        //打印jar包路径
        logger.info("jars_path: " + paths);
        return paths;
    }
}
