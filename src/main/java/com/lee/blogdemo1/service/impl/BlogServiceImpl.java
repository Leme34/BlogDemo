package com.lee.blogdemo1.service.impl;

import com.lee.blogdemo1.entity.*;
import com.lee.blogdemo1.entity.es.EsBlog;
import com.lee.blogdemo1.repository.BlogRepository;
import com.lee.blogdemo1.service.BlogService;
import com.lee.blogdemo1.service.EsBlogService1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class BlogServiceImpl implements BlogService {

    @Autowired
    private BlogRepository blogRepository;

    @Autowired
    private EsBlogService1 esBlogService;


    @Override
    public Optional<Blog> findBlogById(Long id) {
        return blogRepository.findById(id);
    }

    @Transactional
    @Override
    public Blog saveBlog(Blog blog) {
        boolean isNew = (blog.getId() == null);
        EsBlog esBlog = null;

        Blog returnBlog = blogRepository.save(blog);

        //新增blog才新建索引对象
        if (isNew) {
            esBlog = new EsBlog(returnBlog);
        } else {
            //从集群中取出索引对象直接更新对象中的属性
            esBlog = esBlogService.getEsBlogByBlogId(blog.getId());
            esBlog.update(returnBlog);
        }

        //更新集群
        esBlogService.updateEsBlog(esBlog);
        return returnBlog;
    }

    @Transactional
    @Override
    public void removeBlog(Long id) {
        blogRepository.deleteById(id);
    }

    @Override
    public Page<Blog> listBlogByCategory(Category category, Pageable pageable) {
        return blogRepository.findByCategory(category,pageable);
    }

    @Override
    public Page<Blog> listBlogsByUserAndTile(User user, String title, Pageable pageable) {
        title = "%" + title + "%";
        return blogRepository.findByUserAndTitleLike(user,title,pageable);
    }

    @Override
    public Page<Blog> listBlogsByUserAndTileAndNewSort(User user, String title, Pageable pageable) {
        title = "%" + title + "%";
        //根据输入的keyword模糊查询tag和title
        String tags = title;
//        return blogRepository.findByUserAndTagsLikeOrTitleLikeOrderByCreateTimeDesc(user,tags,title,pageable);


        //TODO 为什么一定要传2个user
        return blogRepository.findByTitleLikeAndUserOrTagsLikeAndUserOrderByCreateTimeDesc(title,user,tags,user,pageable);
    }

    @Transactional
    @Override
    public Blog voteBlog(Long id) {

        Optional<Blog> blogOptional = blogRepository.findById(id);
        Blog originalBlog = null;

        //若存在此博文
        if (blogOptional.isPresent()){
            //从上下文中取得当前用户
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            //创建此用户的点赞对象
            Vote vote = new Vote(user);

            //更新博文点赞量
            originalBlog = blogOptional.get();
            boolean isExist = originalBlog.addVote(vote);
            //若此用户的点赞对象已存在点赞列表
            if (isExist){
                throw new IllegalArgumentException("您已经赞过了哦");
            }

        }
        //若不抛异常则保存博文，返回已被此用户点赞的博文对象
       return saveBlog(originalBlog);
    }

    @Transactional
    @Override
    public void removeVote(Long blogId, Long voteId) {

        //查询此博文
        Optional<Blog> blogOptional = blogRepository.findById(blogId);
        if (blogOptional.isPresent()){
            Blog originalBlog = blogOptional.get();
            //移除此博文对象中点赞列表的此点赞对象
            originalBlog.removeVote(voteId);
            //更新数据库blog表的voteSize
            saveBlog(originalBlog);
        }
    }

    @Override
    public void increaseReadSize(Long id) {
        //若博文存在才增加
        Optional<Blog> blogOptional = blogRepository.findById(id);
        if (blogOptional.isPresent()){
            Blog originalBlog = blogOptional.get();
            originalBlog.setReadSize(originalBlog.getReadSize()+1) ;// 原有阅读量+1
            blogRepository.save(originalBlog);
        }
    }

    @Transactional
    @Override
    public Blog createComment(Long blogId,Long pid,User replyUser, String commentContent) {
        //查询该博文对象
        Blog originalBlog = null;
        Optional<Blog> blogOptional = blogRepository.findById(blogId);
        if(blogOptional.isPresent()){
            originalBlog = blogOptional.get();
            //创建该用户的评论对象
            User  user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Comment comment = new Comment(pid,user,replyUser,commentContent,originalBlog);
            //添加到blog对象中,同时使评论量+1
            originalBlog.addComment(comment);
        }
        //此处可以关联更新blog与comment表(用merge权限可能也够)
        return saveBlog(originalBlog);
    }

    @Transactional
    @Override
    public void removeComment(Long blogId, Long commentId) {
        Optional<Blog> blogOptional = blogRepository.findById(blogId);
        if (blogOptional.isPresent()){
            Blog originalBlog = blogOptional.get();
            //从comments中删除此评论,更新评论量
            originalBlog.removeComment(commentId);
            //此处不可以关联更新blog与comment表
            blogRepository.save(originalBlog);
        }
    }
}
