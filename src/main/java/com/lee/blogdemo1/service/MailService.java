package com.lee.blogdemo1.service;

public interface MailService {

    /**
     * 发送带有验证码的邮件到用户的邮箱,并返回该验证码
     */
    String sendMail(String email);

}
