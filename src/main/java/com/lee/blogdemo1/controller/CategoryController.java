package com.lee.blogdemo1.controller;

import com.lee.blogdemo1.VO.CategoryVO;
import com.lee.blogdemo1.VO.Response;
import com.lee.blogdemo1.entity.Category;
import com.lee.blogdemo1.entity.User;
import com.lee.blogdemo1.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 分类控制器.
 */

@Controller
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * 获取分类列表
     * 请求处：js/userspace/u.js
     * @param username : 当前用户博客列表页路径上的username(博主的username)
     */
    @GetMapping
    public String list(@RequestParam(value="username",required=true) String username, Model model) {

        //1、查询博主的所有分类
        User user = (User) userDetailsService.loadUserByUsername(username);
        List<Category> categories = categoryService.listCategory(user);
        System.out.println("博主："+username+" 分类有："+categories);

        //2、判断操作用户是否是博主,传到前端判断是否显示 获取编辑(新增/修改)分类页面的button
        boolean isCategoriesOwner = false;
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        //若未登录则principal.toString()="anonymousUser"
        if (principal!=null && !principal.toString().equals("anonymousUser")){
            User userPrincipal = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (userPrincipal.getUsername().equals(username)) isCategoriesOwner = true;
        }

        model.addAttribute("categories", categories);
        model.addAttribute("isCategoriesOwner", isCategoriesOwner);
        return "/userspace/u :: #catalogRepleace";
    }

    /**
     * 创建分类
     * js/userspace/u.js 提交分类请求提交的是catalogVO
     */
    @PostMapping
    @PreAuthorize("authentication.name.equals(#categoryVO.username)") //当前登录用户是url上的博主用户才能访问,也是使用CategoryVO目的所在
    public ResponseEntity<Response> create(@RequestBody CategoryVO categoryVO) {
        //设置此分类的创建者并入库
        User user = (User) userDetailsService.loadUserByUsername(categoryVO.getUsername());
        Category category = categoryVO.getCategory();
        category.setUser(user);

        categoryService.saveCategory(category);
        return ResponseEntity.ok().body(new Response(true, "处理成功", null));
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("authentication.name.equals(#username)")  // 指定用户才能操作方法
    public ResponseEntity<Response> delete(String username, @PathVariable("id") Long id) {
        categoryService.removeCategoryById(id);
        return ResponseEntity.ok().body(new Response(true, "处理成功", null));
    }

    /**
     * 获取分类编辑界面
     * 由js/userspace/u.js 把返回的html放入 userspace/u.html 的 #categoryFormContainer
     */
    @GetMapping("/edit")
    public String getCategoryEdit(Model model) {
        Category category = new Category(null, null);
        model.addAttribute("category",category);
        return "/userspace/categoryedit";
    }


    /**
     * 根据 Id 获取编辑某个分类界面
     * 由js/userspace/u.js 把返回的html放入 userspace/u.html 的 #categoryFormContainer
     */
    @GetMapping("/edit/{id}")
    public String getCategoryById(@PathVariable("id") Long id, Model model) {
        Category category = null;
        Optional<Category> categoryOptional = categoryService.getCategoryById(id);
        if (categoryOptional.isPresent()){
            category = categoryOptional.get();
        }
        model.addAttribute("category",category);
        return "/userspace/categoryedit";
    }


}
