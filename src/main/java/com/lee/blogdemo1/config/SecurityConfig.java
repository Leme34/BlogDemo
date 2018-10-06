package com.lee.blogdemo1.config;

import com.lee.blogdemo1.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true) //启用@prePostEnabled注解
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String KEY = "com.lee";

    //spring security提供的UserDetailsService,在UserServiceImpl中实现他(用户信息获取服务)的接口loadUserByUsername
    @Autowired
    private UserDetailsService userDetailsService;


    @Bean
    public  PasswordEncoder passwordEncoder(){
        // 使用提供的 BCrypt 加密
        return new BCryptPasswordEncoder();
    }
    //springsecurity5需要新写法
//    @Bean
//    public PasswordEncoder passwordEncoder(){
//        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
//        }

    @Autowired
    private PasswordEncoder passwordEncoder;

    //定义授权规则(权限)
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers("/css/**", "/js/**", "/fonts/**", "/index","/403").permitAll() // 都可以访问
                .antMatchers("/h2-console/**").permitAll() // 都可以访问
                .antMatchers("/admins/**").hasRole("ADMIN") // 需要管理员角色才能访问
                .antMatchers("/users/**").hasRole("ADMIN") // 需要管理员角色才能访问
                .and()
                .formLogin()   //基于 Form 表单登录验证
                .loginPage("/login").failureUrl("/login-error") // 自定义登录界面
                .and().rememberMe().key(KEY) // 启用 remember me
                .and().exceptionHandling().accessDeniedPage("/403");  // 处理异常，拒绝访问就重定向到 403 页面
        http.csrf().ignoringAntMatchers("/h2-console/**"); // 禁用 H2 控制台的 CSRF 防护
        http.headers().frameOptions().sameOrigin(); // 允许来自同一来源的H2 控制台的请求
    }

    //用户认证
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        //踩坑！若没有给roles("角色名")会报错Cannot pass a null GrantedAuthority collection
        //auth.inMemoryAuthentication().withUser("root").password("123").roles("ADMIN");

        //设置验证提供者
        auth.authenticationProvider(authenticationProvider());

        //自定义的UserDetailsService提供自定义用户信息获取服务
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    }

    /**
     * AuthenticationProvider 提供用户UserDetails的具体验证方式，
     * 在其中可以自定义用户密码的加密、验证方式
     */
    @Bean
    public AuthenticationProvider authenticationProvider(){
        //通过dao层提供验证
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        // 设置密码加密方式,用于登录时的密码比对
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return authenticationProvider;
    }

}
