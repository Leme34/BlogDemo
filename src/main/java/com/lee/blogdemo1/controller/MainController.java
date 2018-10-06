package com.lee.blogdemo1.controller;

import com.lee.blogdemo1.entity.Authority;
import com.lee.blogdemo1.entity.Category;
import com.lee.blogdemo1.entity.User;
import com.lee.blogdemo1.service.AuthorityService;
import com.lee.blogdemo1.service.CategoryService;
import com.lee.blogdemo1.service.MailService;
import com.lee.blogdemo1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 主页控制器.
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @since 1.0.0 2017年3月8日
 */
@Controller
public class MainController {

    private static final Long ROLE_USER_AUTHORITY_ID = 2L;

    //保存用户此次发送的邮箱验证码
    private ConcurrentHashMap<String,String> catptchaMap = new ConcurrentHashMap<>();

    @Autowired
    MailService mailService;
    @Autowired
    private UserService userService;
    @Autowired
    private AuthorityService authorityService;
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/")
    public String root() {
        return "redirect:/index";
    }

    @GetMapping("/index")
    public String index() {
        return "redirect:/blogs";
    }

    /**
     * 获取登录界面
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * 用户认证失败
     */
    @GetMapping("/login-error")
    public String loginError(Model model) {
        model.addAttribute("loginError", true);
        model.addAttribute("errorMsg", "登陆失败，用户名或者密码错误！");
        return "login";
    }

    /**
     * 跳转权限拒绝页面
     */
    @RequestMapping("/403")
    public String deny(){
        return "403";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/search")
    public String search() {
        return "search";
    }


    /**
     * 接受点击获取邮箱验证码的请求(ajax)
     */
    @GetMapping("/sendEmail")
    @ResponseBody
    public Map getCaptcha(@RequestParam("email") String email){
        //发邮件进行验证
        String captcha = mailService.sendMail(email);
        //保存到线程安全的Map中
        catptchaMap.put("captcha",captcha);
//        System.out.println("getCaptcha() captcha="+captcha);

        //返回状态码给ajax
        Map map = new HashMap();
        map.put("code", 0);
        return map;
    }


    /**
     * 注册用户,整合邮件验证
     */
    @PostMapping("/registerUser")
    public String registerUser(User user,     //默认注入bean相同字段的属性
                               @RequestParam("captcha") String captcha,
                               RedirectAttributes redirectAttributes,
                               ModelMap modelMap) {
        //检查邮箱验证码是否匹配
        if (captcha.equals(catptchaMap.get("captcha"))) {

            List<Authority> authorities = new ArrayList<>();
            //从数据库中查到默认权限,并加入权限列表
            authorities.add(authorityService.getAuthorityById(ROLE_USER_AUTHORITY_ID));
            //设置用户的(多个)权限
            user.setAuthorities(authorities);


            //捕获唯一键约束异常
            try {
                //加密密码并入库
                userService.registerUser(user);
                //为此用户添加一个默认分类(必须先保存用户)
                Category category = new Category(user,"默认分类");
                categoryService.saveCategory(category);

            }catch (DataIntegrityViolationException e){
                String msg = e.getRootCause().getMessage();
                if (msg.startsWith("Duplicate entry")) {
                    msg = msg.substring(msg.indexOf("'"), msg.indexOf("for")) + "已被注册!";
                }
                modelMap.addAttribute("msg", msg);
                return "/register";
            }


            redirectAttributes.addFlashAttribute("msg", "注册成功!");
            return "redirect:/login";
        }else {
            modelMap.addAttribute("msg", "邮箱验证失败!");
            return "/register";
        }

    }


}
