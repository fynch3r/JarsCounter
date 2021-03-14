package com.sec.jc.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * @program: JarsCounter
 * @description:
 * @author: 0range
 * @create: 2021-03-14 13:45
 **/


public class JarsUtils {

    public static String EXPLODED_JARS_PATH = String.join(File.separator, System.getProperty("user.dir"), "exploded-jars");

    private static Logger logger = LoggerFactory.getLogger(JarsUtils.class);

    public static ClassLoader getJarClassLoader(List<Path> jarPathUrls) throws IOException {
        List<URL> classPathUrls = new ArrayList<>(jarPathUrls.size());
        for (Path jarPath : jarPathUrls) {
            Path jarRealPath = jarPath.toAbsolutePath();
            if (!Files.exists(jarRealPath)) {
                throw new IllegalArgumentException("Path \"" + jarPath + "\" is not a path to a file.");
            }
            classPathUrls.add(jarRealPath.toUri().toURL());
        }
        return new URLClassLoader(classPathUrls.toArray(new URL[0]));
    }

    public static void deleteDirectory(Path root) throws IOException {
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        final byte[] buffer = new byte[4096];
        int n;
        while ((n = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, n);
        }
    }

    public static void Jar2TempDir(Path jarPath,Path tempRootDir) throws IOException {

        String[] tempName = jarPath.toString().split(File.separator);

        String jarName = tempName[tempName.length-1].replaceAll("\\.","-");

        final Path tmpDir = Files.createDirectory(Paths.get(tempRootDir + File.separator + jarName.substring(0,jarName.lastIndexOf("-"))));

        // 将jar包内容提取到临时文件夹
        try (JarInputStream jarInputStream = new JarInputStream(Files.newInputStream(jarPath))) {
            JarEntry jarEntry;
            while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
                Path fullPath = tmpDir.resolve(jarEntry.getName());
                if (!jarEntry.isDirectory()) {
                    Path dirName = fullPath.getParent();
                    if (dirName == null) {
                        throw new IllegalStateException("Parent of item is outside temp directory.");
                    }
                    if (!Files.exists(dirName)) {
                        Files.createDirectories(dirName);
                    }
                    try (OutputStream outputStream = Files.newOutputStream(fullPath)) {
                        copy(jarInputStream, outputStream);
                    }
                }
            }
        }
    }

}
