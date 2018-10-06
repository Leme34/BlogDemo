package com.lee.blogdemo1.utils;

import java.util.Random;

public class IdUtil {

    public static String generateId(){
        //生成策略：4位随机数+时间戳
        Random random = new Random();
        int num = random.nextInt(9000) + 1000;
        return System.currentTimeMillis() + num + "";
    }

}
