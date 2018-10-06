package com.lee.blogdemo1.controller;

import com.lee.blogdemo1.VO.Response;
import com.lee.blogdemo1.entity.Blog;
import com.lee.blogdemo1.entity.Category;
import com.lee.blogdemo1.entity.User;
import com.lee.blogdemo1.entity.Vote;
import com.lee.blogdemo1.service.BlogService;
import com.lee.blogdemo1.service.CategoryService;
import com.lee.blogdemo1.service.EsBlogService;
import com.lee.blogdemo1.service.UserService;
import com.lee.blogdemo1.utils.IdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

/**
 * 用户主页空间控制器.
 */
@Controller
@RequestMapping("/u")
public class UserspaceController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BlogService blogService;
    @Autowired
    private EsBlogService esBlogService;

    //从配置文件注入
    @Value("${file.server.url}")
    private String fileServerUrl;

    //从配置文件注入
    @Value("${file.server.deleteUrl}")
    private String fileServerDeleteUrl;

    /**
     * 跳转用户的个人主页
     */
    @GetMapping("/{username}")
    public String userSpace(@PathVariable("username") String username, Model model) {
        //请求用户主页博客列表
        return "redirect:/u/" + username + "/blogs";
    }

    /**
     * 获取用户个人主页的博客列表
     */
    @GetMapping("/{username}/blogs")
    public String listBlogsByOrder(@PathVariable("username") String username,
                                   @RequestParam(value = "order", required = false, defaultValue = "new") String order,
                                   @RequestParam(value = "category", required = false) Long categoryId,
                                   @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
                                   @RequestParam(value = "async", required = false) boolean async,
                                   @RequestParam(value = "pageIndex", required = false, defaultValue = "0") int pageIndex,
                                   @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize, Model model) {

        User user = (User) userDetailsService.loadUserByUsername(username);
        System.out.println("==============="+user);
        Page<Blog> page = null;
        //按分类或者最新或最热查询3选1

        if (categoryId != null && categoryId > 0) { //分类查询，判断categoryId是否合法
            //先根据分类id查询是否存在
            Optional<Category> categoryOptional = categoryService.getCategoryById(categoryId);
            //若有此分类则分类查询
            if (categoryOptional.isPresent()) {
                Pageable pageable = new PageRequest(pageIndex, pageSize);
                page = blogService.listBlogByCategory(categoryOptional.get(), pageable);
            }
        } else if (order.equals("hot")) {  //最热排序查询(阅读量、评论数、点赞数降序)
            Sort sort = new Sort(Sort.Direction.DESC, "readSize", "commentSize", "voteSize"); //多字段排序用sort对象
            Pageable pageable = new PageRequest(pageIndex, pageSize, sort);
            //排序规则已在pageable传入,只需要限定user、title关键字即可
            page = blogService.listBlogsByUserAndTile(user, keyword, pageable);
        } else if (order.equals("new")) {  //最新排序查询
            Pageable pageable = new PageRequest(pageIndex, pageSize);
            page = blogService.listBlogsByUserAndTileAndNewSort(user, keyword, pageable);  //单字段排序在repository声明即可实现
        }
        List<Blog> list = page.getContent(); // 当前所在页面数据列表


        model.addAttribute("user", user);
        model.addAttribute("order", order);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("keyword", keyword);
        model.addAttribute("page", page);
        model.addAttribute("blogList", list);
        return (async == true ? "/userspace/u :: #mainContainerRepleace" : "/userspace/u");
    }


    /**
     * 获取博客详细展示页
     */
    @GetMapping("/{username}/blogs/{id}")
    public String getBlogById(@PathVariable("username") String username, @PathVariable("id") Long id, Model model, HttpServletRequest request) {
        Blog blog = null;
        Optional<Blog> blogOptional = blogService.findBlogById(id);
        //若博文存在则赋值,否则传空对象到前端
        if (blogOptional.isPresent()) {
            blog = blogOptional.get();
        }

        //1、增加阅读量
        blogService.increaseReadSize(id);

        //2、获取当前用户
        User user = null;
        if (!SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString().equals("anonymousUser")){  //若已登录
            user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        }

        //3、赋值当前用户对此文章点赞状态
        Vote currentVote = null;
        if (user != null) {
            //遍历该博文点赞的用户
            for (Vote vote : blog.getVotes()) {
                //若找到此用户的点赞对象则赋值
                if (vote.getUser().getUsername().equals(user.getUsername())) {
                    currentVote = vote;
                    break;
                }
            }
        }

        //4、当前用户是否该博文的所有者
        boolean isBlogOwner = false;
        if (user != null && username.equals(user.getUsername())) {
            isBlogOwner = true;
        }

        model.addAttribute("currentVote", currentVote);    //点赞状态
        model.addAttribute("isBlogOwner", isBlogOwner);  //是否此博客的所有者
        model.addAttribute("blogModel", blog);
        model.addAttribute("fileServerDeleteUrl", fileServerDeleteUrl);  //为删除按钮保存文件服务器删除的url


        return "/userspace/blog";
    }

    /**
     * 获取新增博客的界面
     */
    @GetMapping("/{username}/blogs/edit")
    public ModelAndView createBlog(@PathVariable("username") String username, Model model) {
        // 获取用户分类列表
        User user = (User) userDetailsService.loadUserByUsername(username);
        List<Category> categories = categoryService.listCategory(user);

        model.addAttribute("categories", categories);
        //传入一个空的博文对象
        model.addAttribute("blog", new Blog(null, null, null, IdUtil.generateId()));
        model.addAttribute("fileServerUrl", fileServerUrl);// 文件服务器的地址返回给前端保存
        return new ModelAndView("/userspace/blogedit", "blogModel", model);
    }


    /**
     * 保存博客(修改或新增)
     */
    @PostMapping("/{username}/blogs/edit")
    @PreAuthorize("authentication.name.equals(#username)")  //认证是否当前用户
    public ResponseEntity<Response> saveBlog(@PathVariable("username") String username, @RequestBody Blog blog) {

        //若未选分类则返回错误信息
        if (blog.getCategory().getId() == null) {
            return ResponseEntity.ok().body(new Response(false, "未选择分类"));
        }

        //若blogId不为空是修改博文
        if (blog.getId() != null) {
            Optional<Blog> blogOptional = blogService.findBlogById(blog.getId());
            //若博文存在则更新可修改字段
            if (blogOptional.isPresent()) {
                Blog originalBlog = blogOptional.get();
                originalBlog.setTitle(blog.getTitle());
                originalBlog.setContent(blog.getContent());
                originalBlog.setSummary(blog.getSummary());
                originalBlog.setCategory(blog.getCategory()); // 增加对分类的处理
                originalBlog.setTags(blog.getTags());  // 增加对标签的处理
                blogService.saveBlog(originalBlog);
            }
        } else {  //否则blogId=null是新增博文
            //设置创建者
            User user = (User) userDetailsService.loadUserByUsername(username);
            blog.setUser(user);
            blogService.saveBlog(blog);
        }
        //返回此博文页面url
        String redirectUrl = "/u/" + username + "/blogs/" + blog.getId();
        return ResponseEntity.ok().body(new Response(true, "处理成功", redirectUrl));
    }


    /**
     * 获取编辑博客的界面
     */
    @GetMapping("/{username}/blogs/edit/{id}")
    @PreAuthorize("authentication.name.equals(#username)")
    public ModelAndView editBlog(@PathVariable("username") String username,
                                 @PathVariable("id") Long id, Model model) {

        //查询此用户分类列表
        User user = (User) userDetailsService.loadUserByUsername(username);
        List<Category> categoryList = categoryService.listCategory(user);

        model.addAttribute("categories", categoryList);
        //查询待编辑的博文传过去
        model.addAttribute("blog", blogService.findBlogById(id).get());
        model.addAttribute("fileServerUrl", fileServerUrl);// 文件服务器的地址返回给客户端
        return new ModelAndView("/userspace/blogedit", "blogModel", model);
    }


    /**
     * 删除博客,并从es集群删除
     * 最后应该发请求删除文件服务器中此博客插入的图片
     */
    @DeleteMapping("/{username}/blogs/{id}")
    @PreAuthorize("authentication.name.equals(#username)")
    public ResponseEntity<Response> deleteBlog(@PathVariable("username") String username, @PathVariable("id") Long id) {

        blogService.removeBlog(id);
        //TODO 从es集群删除，是否需要查找es中有没有？
        esBlogService.removeByBlogId(id);

        //返回用户博文列表页
        String redirectUrl = "/u/" + username + "/blogs";
        return ResponseEntity.ok().body(new Response(true, "处理成功", redirectUrl));
    }


//==============================================用户信息设置相关=============================================

    /**
     * 获取个人设置页面
     */
    @GetMapping("/{username}/profile")
    @PreAuthorize("authentication.name.equals(#username)")  //验证用户是否操作自己的账户,否则拒绝访问
    public ModelAndView profile(@PathVariable("username") String username, Model model) {
        //查询用户信息并返回
        User user = (User) userDetailsService.loadUserByUsername(username);
        model.addAttribute("user", user);
        model.addAttribute("fileServerUrl", fileServerUrl);// 文件服务器的上传文件的url返回给客户端
        return new ModelAndView("/userspace/profile", "userModel", model);
    }

    /**
     * 保存个人设置
     * 传入的user没有头像url，要从数据库中查出头像url赋值到其中，否则更换了头像之后再点击保存会覆盖掉头像url为空
     */
    @PostMapping("/{username}/profile")
    @PreAuthorize("authentication.name.equals(#username)")
    public String saveProfile(@PathVariable("username") String username, User user, RedirectAttributes redirectAttributes) {
        User originalUser = userService.getUserById(user.getId()).get();
        //1、赋值头像url
        user.setAvatar(originalUser.getAvatar());


        //2、新密码编码后与原密码匹配
        String encodedPwd = passwordEncoder.encode(user.getPassword());
        System.out.println("encodedPwd:"+encodedPwd+","+",originalUser.getPassword()="+originalUser.getPassword());
        String rawPwd = originalUser.getPassword();
        //若不匹配则密码先编码再更新user，否则直接更新user
        if (!passwordEncoder.matches(rawPwd, encodedPwd)) {
            user.setPassword(encodedPwd);
        }
        //3、捕获唯一键约束异常
        try {
            userService.saveOrUpateUser(user);
        } catch (DataIntegrityViolationException e) {
            System.out.println("抛异常！！！！！！！111111");
            String msg = e.getRootCause().getMessage();
            if (msg.startsWith("Duplicate entry")) {
                msg = msg.substring(msg.indexOf("'"), msg.indexOf("for")) + "已被注册!";
            }
            //多个controller映射同一个地址(不同请求方法)必须用重定向才能跳转
            redirectAttributes.addFlashAttribute("msg", msg);
            return "redirect:/u/" + username + "/profile";
        }

        return "redirect:/u/" + username + "/profile";
    }


    /**
     * 获取编辑头像的界面
     * ajax请求：static/js/userspace/main.js
     */
    @GetMapping("/{username}/avatar")
    @PreAuthorize("authentication.name.equals(#username)")
    public ModelAndView avatar(@PathVariable("username") String username, Model model) {
        User user = (User) userDetailsService.loadUserByUsername(username);
        //返回的视图数据放入profile.html的avatarFormContainer中
        model.addAttribute("user", user);
        return new ModelAndView("/userspace/avatar", "userModel", model);
    }

    /**
     * 保存(更新)头像url
     * ajax请求：static/js/userspace/main.js 在头像上传文件服务器成功后请求
     */
    @PostMapping("/{username}/avatar")
    @PreAuthorize("authentication.name.equals(#username)")
    public ResponseEntity<Response> saveAvatar(@PathVariable("username") String username,
                                               @RequestParam("userId") Long userId,  //不只可以接受String类型
                                               @RequestParam("avatarUrl") String avatarUrl) {
        //必须要用数据库中的user更新,因为只改变头像url而不改变其他属性
        User originalUser = userService.getUserById(userId).get();
        //更新头像url
        originalUser.setAvatar(avatarUrl);

        userService.saveOrUpateUser(originalUser);

        //返回新的头像url,用于js置换头像图片
        return ResponseEntity.ok().body(new Response(true, "处理成功", avatarUrl));
    }


}
