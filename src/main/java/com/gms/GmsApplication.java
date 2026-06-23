package com.gms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.gms.mapper")
public class GmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(GmsApplication.class, args);
    }
}
/**
 *                       _oo0oo_
 *                      o8888888o
 *                      88" . "88
 *                      (| -_- |)
 *                      0\  =  /0
 *                    ___/`---'\___
 *                  .' \\|     |// '.
 *                 / \\|||  :  |||// \
 *                / _||||| -:- |||||- \
 *               |   | \\\  -  /// |   |
 *               | \_|  ''\---/''  |_/ |
 *               \  .-\__  '-'  ___/-. /
 *             ___'. .'  /--.--\  `. .'___
 *          ."" '<  `.___\_<|>_/___.' >' "".
 *         | | :  `- \`. ;`. _/ ;.' /-' : | |
 *         \  \_. \_  \_.\_)_(/_ ._/ _/ ._/  /
 *      ====`-.`___`-.__\___/___.-'___'.-'===
 *                    `=---='
 *
 *        佛祖保佑        永不报错        代码无BUG
 *                    期末成绩满分
 */