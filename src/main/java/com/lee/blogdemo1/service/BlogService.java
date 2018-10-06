package com.lee.blogdemo1.service;

import com.lee.blogdemo1.entity.Blog;
import com.lee.blogdemo1.entity.Category;
import com.lee.blogdemo1.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BlogService {

    //根据id查询博文
    Optional<Blog> findBlogById(Long id);

    //保存博文
    Blog saveBlog(Blog blog);


    //删除博文
    void removeBlog(Long id);

    /**
     * 根据分类查询博文
     */
    Page<Blog> listBlogByCategory(Category category, Pageable pageable);

    /**
     * 根据 用户 博文名称 进行分页模糊查询（最热，多字段排序规则用Sort对象实现）
     */
    Page<Blog> listBlogsByUserAndTile(User user, String title, Pageable pageable);

    /**
     * 根据 用户 博文名称和标签 创建时间降序 进行分页模糊查询（最新）
     */
    Page<Blog> listBlogsByUserAndTileAndNewSort(User user, String title, Pageable pageable);


    /**
     * 根据id点赞博文
     */
    Blog voteBlog(Long id);

    /**
     * 根据博文id，和voteId取消点赞
     */
    void removeVote(Long blogId,Long voteId);

    /**
     * 增加此博文阅读量
     */
    void increaseReadSize(Long id);

    /**
     * 增加一条该博文的评论,返回增加评论后的blog对象
     * 因为blog的comments属性用了jpa关联注解，所以jpa会更新comment表
     */
    Blog createComment(Long blogId,Long pid,User replyUser, String commentContent);

    /**
     * 删除一条该博文的评论
     */
    void removeComment(Long blogId,Long commentId);


}
