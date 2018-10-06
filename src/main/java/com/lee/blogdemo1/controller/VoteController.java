package com.lee.blogdemo1.controller;

import com.lee.blogdemo1.VO.Response;
import com.lee.blogdemo1.entity.User;
import com.lee.blogdemo1.entity.Vote;
import com.lee.blogdemo1.service.BlogService;
import com.lee.blogdemo1.service.CommentService;
import com.lee.blogdemo1.service.VoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * 点赞相关操作
 */

@Controller
@RequestMapping("/votes")
public class VoteController {

    @Autowired
    private BlogService blogService;

    @Autowired
    private VoteService voteService;

    @Autowired
    private CommentService commentService;

    /**
     * 发表博客点赞
     * 请求处：/js/userspace/blog.js中点击点赞按钮
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')") // 指定角色权限才能操作方法
    public ResponseEntity<Response> voteBlog(Long blogId) {
        //由异常处理器捕获此用户已为此博文点过赞抛出的异常
        blogService.voteBlog(blogId);
        return ResponseEntity.ok().body(new Response(true,"点赞成功",null));
    }


    /**
     * 取消博客点赞
     * 请求处：/js/userspace/blog.js中点击取消点赞按钮
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")  // 指定角色权限才能操作方法
    public ResponseEntity<Response> removeVoteBlog(@PathVariable("id") Long id, Long blogId) {
        User voteUser = null;
        boolean isOwner = false;

        //判断是否存在此点赞
        Optional<Vote> voteOptional = voteService.findById(id);
        if (!voteOptional.isPresent()){
            return ResponseEntity.ok().body(new Response(false, "不存在该点赞记录！"));
        }

        //若用户已登录
        if (!SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString().equals("anonymousUser")){
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();  //当前登录的用户
            voteUser = voteOptional.get().getUser();   //点赞的用户
            //若当前登录用户与点赞者的用户名匹配
            if (user!=null && user.getUsername().equals(voteUser.getUsername())){
                isOwner = true;
            }
        }

        if (isOwner){
            //删除此博文的此用户点赞
            blogService.removeVote(blogId,id);
            voteService.removeById(id);
            return ResponseEntity.ok().body(new Response(true, "取消点赞成功", null));
        }else
            return ResponseEntity.ok().body(new Response(false, "没有操作权限"));
    }


    /**
     * 点赞评论
     */
    @PostMapping("/comment")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity voteComment(Long commentId){
        //找不到评论异常已在Service层处理
        commentService.voteComment(commentId);
        return ResponseEntity.ok().body(new Response(true,"点赞成功",null));
    }



    /**
     * 取消点赞评论
     */
    @DeleteMapping("/comment")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')") //未登录不能进入此方法内
    public ResponseEntity removeVoteComment(Long commentId){
        User user = null;
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString().equals("anonymousUser")){
            return ResponseEntity.ok().body(new Response(false, "请先登录"));
        }else {
            user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        }

        //1、查出是否存在此用户对此评论的vote对象
        Optional<Vote> voteOptional = voteService.findByUserAndComment(user.getId(),commentId);
        if (!voteOptional.isPresent()){
            return ResponseEntity.ok().body(new Response(false, "没有操作权限"));
        }

        System.out.println("==============voteId="+voteOptional.get().getId());

        //删除此vote
        //commentService.removeVote(commentId, voteOptional.get().getId()); //只是从comment对象的voteList中删除此vote，并不能关联删除vote
        voteService.removeById(voteOptional.get().getId());
        return ResponseEntity.ok().body(new Response(true, "取消点赞成功", null));

//        //2、当前用户是否点赞用户
//        boolean isOwner = false;
//        if (!SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString().equals("anonymousUser")){
//            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//            if (vote.getUser().getUsername().equals(user.getUsername())){
//                isOwner = true;
//            }
//        }

//        if (isOwner) {
//            //方法中检查是否有该评论
//            commentService.removeVote(commentId, id);
//            return ResponseEntity.ok().body(new Response(true, "取消点赞成功", null));
//        }else {
//            return ResponseEntity.ok().body(new Response(false, "没有操作权限"));
//        }
    }



}
