package com.lee.blogdemo1.service.impl;

import com.lee.blogdemo1.entity.Blog;
import com.lee.blogdemo1.entity.Comment;
import com.lee.blogdemo1.entity.User;
import com.lee.blogdemo1.entity.Vote;
import com.lee.blogdemo1.repository.CommentRepository;
import com.lee.blogdemo1.service.CommentService;
import com.lee.blogdemo1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserService userService;

    @Override
    public Optional<Comment> getCommentById(Long id) {
        return commentRepository.findById(id);
    }

    @Transactional
    @Override
    public void removeComment(Long id) {
        commentRepository.deleteById(id);
    }

    @Override
    public List<Comment> findAllByBlog(Blog blog) {
        return commentRepository.findAllByBlog(blog);
    }

    @Transactional
    @Override
    public void voteComment(Long id) {
        //1、评论是否存在
        Optional<Comment> commentOptional = commentRepository.findById(id);
        if (!commentOptional.isPresent()) {
            throw new IllegalArgumentException("不存在该评论");
        }
        Comment comment = commentOptional.get();

        //此用户是否已点过赞
        boolean isExist = false;

        //创建此用户给此博文点的vote
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Vote vote = new Vote(user, comment);
        //加入voteList
        isExist = comment.addVote(vote);

        if (isExist) throw new IllegalArgumentException("您已经赞过了哦");
        else {
            //使用merge级联保存vote,因为comment的voteList属性修改了那么comment对象保存时同时需要(合并)修改votes里的对象再保存
            commentRepository.save(comment);
        }
    }

    @Transactional
    @Override
    public void removeVote(Long commentId,Long voteId) {
        Comment comment = commentRepository.findById(commentId).get();
        comment.removeVote(voteId);
        commentRepository.save(comment);
    }


}
