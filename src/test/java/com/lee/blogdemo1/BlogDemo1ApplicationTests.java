package com.lee.blogdemo1;

import com.lee.blogdemo1.entity.Blog;
import com.lee.blogdemo1.entity.Comment;
import com.lee.blogdemo1.repository.CommentRepository;
import com.lee.blogdemo1.service.BlogService;
import com.lee.blogdemo1.service.CommentService;
import com.lee.blogdemo1.service.VoteService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BlogDemo1ApplicationTests {

	@Autowired
	private BlogService blogService;

	@Autowired
	private CommentService commentService;

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private VoteService voteService;

	@Test
	public void contextLoads() {
		voteService.removeById(Long.valueOf(14));

	}

}
