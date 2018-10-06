package com.lee.blogdemo1.controller;

import com.lee.blogdemo1.VO.Response;
import com.lee.blogdemo1.entity.Blog;
import com.lee.blogdemo1.entity.Comment;
import com.lee.blogdemo1.entity.User;
import com.lee.blogdemo1.entity.Vote;
import com.lee.blogdemo1.service.BlogService;
import com.lee.blogdemo1.service.CommentService;
import com.lee.blogdemo1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 评论控制器
 * 若pid=0则是一级评论 ,若replyUser=null则是二级评论,若replyUser!=null则是二级评论的子评论
 */

@Controller
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private BlogService blogService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;
    /**
     * 获取评论列表
     */
    @GetMapping
    public String listComments(@RequestParam(value = "blogId") Long blogId, Model model) {
        List<Comment> comments = null;
        //1、获取此博文的评论
        Optional<Blog> blogOptional = blogService.findBlogById(blogId);//blog类的comments属性上用了jpa关联注释，所以查询blog的同时会查出comments,comment级联查询出此评论点赞状态,但是hibernate can't fetch the two collections with one query，需要在comment类的voteList上去掉fetchType并加上@LazyCollection(LazyCollectionOption.FALSE)
        if (blogOptional.isPresent()){
            comments = blogOptional.get().getComments();
        }

        //2、当前访问用户是否评论者(若是则可以删除该评论)
        String currentUser = ""; //当前用户名
        User user = null;
        //若已登录
        if(!SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString().equals("anonymousUser")){
            user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(); //当前用户
        }

        if (user!=null){
            //当前登录用户传到前端与评论者比对，来判断是否显示删除评论按钮
            currentUser = user.getUsername();
        }

//        List<Long> commentIdList = new ArrayList<>();
//        for (Comment comment : comments){
//            for (Vote vote : comment.getVoteList()){
//                if (vote.getUser().getUsername().equals(currentUser)){
//                    commentIdList.add(comment.getId());
//                }
//            }
//        }
        //当前用户点赞的所有评论的id集合(不能使用comments遍历，因为其中只是blog关联查询的pid=0的comment)
        List<Comment> commentList = commentService.findAllByBlog(blogOptional.get());
        List<Long> commentIdList = new ArrayList<>();
        for (Comment comment : commentList){
            for (Vote vote : comment.getVoteList()){
                if (vote.getUser().getUsername().equals(currentUser)){
                    commentIdList.add(comment.getId());
                }
            }
        }

//        System.out.println("当前用户所赞的commentId有："+commentIdList);
        model.addAttribute("comments", comments);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("commentIdList", commentIdList);
        return "/userspace/blog :: #mainContainerRepleace";
    }

    /**
     * 发表评论
     * hasRole('HR')和 hasAuthority('ROLE_HR')是相同的新版本Spring不让在hasRole中使用 ROLE_ 前缀
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")  //只有这些角色才能评论
    public ResponseEntity<Response> createComment(Long blogId,
                                                  @RequestParam(value = "pid",required = false,defaultValue = "0")String pid,
                                                  @RequestParam(value = "replyUserId",required = false,defaultValue = "0")String replyUserId,  // 若不写defaultValue，required=false表示不传的话,会给参数赋值为null
                                                  String commentContent) {
        User replyUser = null;
        Optional<User> userOptional = userService.getUserById(Long.parseLong(replyUserId));
        if (userOptional.isPresent()){
            replyUser = userOptional.get();
        }
        //评论人(当前用户)在service层搞定
        blogService.createComment(blogId,Long.parseLong(pid),replyUser,commentContent);
        return ResponseEntity.ok().body(new Response(true, "处理成功", null));
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<Response> delete(@PathVariable("id") Long id, Long blogId) {
        Comment comment = null;
        Optional<Comment> commentOptional = commentService.getCommentById(id);
        //1、若不存在该评论直接返回错误
        if (!commentOptional.isPresent()){
            return ResponseEntity.ok().body(new Response(false, "不存在该评论！"));
        }
        comment = commentOptional.get();

        //2、判断是否当前用户是否评论者
        User user = comment.getUser();//评论者
        User principal= (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(); //当前用户
        //若不是评论者则返回错误
        if (!user.getUsername().equals(principal.getUsername())){
            return ResponseEntity.ok().body(new Response(false, "没有操作权限"));
        }

    //3、删除该评论

        //先删除此评论的所有子评论
        List<Comment> commentList = comment.getCommentList();
        if (commentList != null && commentList.size() != 0) {
            for (Comment c : commentList) {
                commentService.removeComment(c.getId());
            }
        }
        //再删除此评论
        commentService.removeComment(comment.getId());
        //此博客commentSize-1
        blogService.removeComment(blogId,id);

        return ResponseEntity.ok().body(new Response(true, "处理成功", null));
    }



}
