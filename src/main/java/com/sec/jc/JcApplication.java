package com.sec.jc;

import ch.qos.logback.core.util.FileUtil;
import com.sec.jc.core.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.Resource;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class JcApplication implements CommandLineRunner {

    @Resource
    private ApplicationArguments arguments;

    private Path target;

    private boolean dirflag = false;

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(JcApplication.class);
        //是否显示Spring欢迎界面,OFF不显示欢迎界面
        app.setBannerMode(Banner.Mode.OFF);
        //启动
        app.run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        Logger logger = LoggerFactory.getLogger(JcApplication.class);
        logger.info("Start Counter!");

        try {
            if(this.arguments.containsOption("toDir")){
                this.dirflag = true;
            }
            if(this.arguments.getNonOptionArgs().size() == 1){
                this.target = Paths.get(this.arguments.getNonOptionArgs().get(0));
                Parser.analysis(this.target,this.dirflag);
            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }
}
