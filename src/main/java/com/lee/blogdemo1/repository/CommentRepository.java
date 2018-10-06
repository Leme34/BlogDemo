package com.lee.blogdemo1.repository;

import com.lee.blogdemo1.entity.Blog;
import com.lee.blogdemo1.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment,Long>{

    /**
     * 根据id查询comment
     */
    Optional<Comment> findById(Long id);

    /**
     * 查询此用户点赞的所有评论
     */
    List<Comment> findAllByBlog(Blog blog);
}
