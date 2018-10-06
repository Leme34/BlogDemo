package com.lee.blogdemo1.service.impl;

import com.lee.blogdemo1.entity.Comment;
import com.lee.blogdemo1.entity.User;
import com.lee.blogdemo1.entity.Vote;
import com.lee.blogdemo1.repository.VoteRepository;
import com.lee.blogdemo1.service.CommentService;
import com.lee.blogdemo1.service.UserService;
import com.lee.blogdemo1.service.VoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class VoteServiceImpl implements VoteService {

    @Autowired
    private VoteRepository voteRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private CommentService commentService;

    @Override
    public Optional<Vote> findById(Long id) {
        return voteRepository.findById(id);
    }

    @Transactional
    @Override
    public void removeById(Long id) {
        voteRepository.deleteById(id);
    }

    @Override
    public Optional<Vote> findByUserAndComment(Long userId, Long commentId) {
        User user = userService.getUserById(userId).get();
        Comment comment = commentService.getCommentById(commentId).get();
        return voteRepository.findByUserAndAndComment(user,comment);
    }


}
