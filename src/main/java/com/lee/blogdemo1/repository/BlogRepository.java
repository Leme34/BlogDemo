package com.lee.blogdemo1.repository;

import com.lee.blogdemo1.entity.Blog;
import com.lee.blogdemo1.entity.Category;
import com.lee.blogdemo1.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlogRepository extends JpaRepository<Blog,Long> {

    //根据id查询博文
    Optional<Blog> findById(Long id);

    /**
     * 根据 博文分类 分页查询博文
     */
    Page<Blog> findByCategory(Category category, Pageable pageable);

    /**
     * 根据 用户 博文名称 进行分页模糊查询（最热，多字段排序规则用Sort对象实现）
     */
    Page<Blog> findByUserAndTitleLike(User user,String tsitle,Pageable pageable);

    /**
     * 根据 用户 博文名称和标签 创建时间降序 进行分页模糊查询（最新）findByUserAndTitleLikeOrTagsLikeAndOrderByCreateTimeDesc
    */
//    Page<Blog> findByUserAndTagsLikeOrTitleLikeOrderByCreateTimeDesc(User user,String tag,String title,Pageable pageable);
    Page<Blog> findByTitleLikeAndUserOrTagsLikeAndUserOrderByCreateTimeDesc(String title,
                                                                            User user, String tags, User user2, Pageable pageable);

}
