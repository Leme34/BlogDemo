package com.lee.blogdemo1.service;

import com.lee.blogdemo1.VO.TagVO;
import com.lee.blogdemo1.entity.User;
import com.lee.blogdemo1.entity.es.EsBlog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EsBlogService {

    /**
     * 更新 EsBlog
     */
    EsBlog updateEsBlog(EsBlog esBlog);

    /**
     * 按最热查询
     */
    Page<EsBlog> listHotestEsBlogs(String keyword, Pageable pageable);

    /**
     * 按最热查询5条
     */
    List<EsBlog> listTop5HotestEsBlogs();

    /**
     * 最新查询
     */
    Page<EsBlog> listNewestEsBlogs(String keyword, Pageable pageable);

    /**
     * 最新查询5条
     */
    List<EsBlog> listTop5NewestEsBlogs();

    /**
     * 查询所有
     */
    Page<EsBlog> listEsBlogs(Pageable pageable);

    /**
     * 最热前 30 标签
     */
    List<TagVO> listTop30Tags();

    /**
     * 最热前12用户
     */
    List<User> listTop12Users();

    /**
     * 根据id查询
     */
    EsBlog getEsBlogByBlogId(Long id);

    /**
     * 根据id从es集群删除
     */
    void removeByBlogId(Long id);

}
