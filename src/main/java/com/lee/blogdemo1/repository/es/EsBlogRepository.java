package com.lee.blogdemo1.repository.es;

import com.lee.blogdemo1.entity.es.EsBlog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * EsBlog Repository接口.
 * 
 */
public interface EsBlogRepository extends ElasticsearchRepository<EsBlog, String> {
	
    /**
     * 模糊查询(去重)
     */
    Page<EsBlog> findByTitleContainingOrSummaryContainingOrContentContainingOrTagsContaining(
            String title, String summary, String content, String tags, Pageable pageable);

    /**
     * 根据 Blog 的id 查询 EsBlog
     */
    EsBlog findByBlogId(Long blogId);

    /**
     * 根据 Blog 的id 查询 EsBlog
     */
    void deleteByBlogId(Long blogId);
}
