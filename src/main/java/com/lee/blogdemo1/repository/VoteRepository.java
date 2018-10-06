package com.lee.blogdemo1.repository;

import com.lee.blogdemo1.entity.Comment;
import com.lee.blogdemo1.entity.User;
import com.lee.blogdemo1.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote,Long>{

    Optional<Vote> findById(Long id);

    /**
     * 查询此用户对此评论的vote对象
     */
    Optional<Vote> findByUserAndAndComment(User user, Comment comment);
}
