package com.lee.blogdemo1.service;

import com.lee.blogdemo1.entity.Vote;

import java.util.Optional;

public interface VoteService {

    //根据id查询点赞记录
    Optional<Vote> findById(Long id);

    //根据id删除点赞记录
    void removeById(Long id);

    /**
     * 查询此用户对此评论的vote对象
     */
    Optional<Vote> findByUserAndComment(Long userId,Long commentId);

}
