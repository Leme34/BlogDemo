package com.lee.blogdemo1.service;

import com.lee.blogdemo1.entity.Blog;
import com.lee.blogdemo1.entity.Comment;
import com.lee.blogdemo1.entity.User;

import java.util.List;
import java.util.Optional;

public interface CommentService {

    /**
     * 查询此博文的所有评论
     */
    Optional<Comment> getCommentById(Long id);

    /**
     *  根据id删除评论
     */
    void removeComment(Long id);

    /**
     * 根据id点赞评论
     */
    void voteComment(Long id);

    /**
     * 取消点赞
     */
    void removeVote(Long commentId,Long voteId);

    /**
     * 查询此用户点赞的所有评论(不只是blog关联查出的pid=0的)
     */
    List<Comment> findAllByBlog(Blog blog);
}
