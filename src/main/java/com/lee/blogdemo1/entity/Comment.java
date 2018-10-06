package com.lee.blogdemo1.entity;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.validator.constraints.NotEmpty;
import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * Comment 实体
 * pid：评论的父id，注意A评论下的所有子评论，即回复的回复,回复的回复的回复的pid都是某个一级评论
 * replyUser：被回复人的id，用于@对方时候显示。对一级评论进行评论不需要@他，所以reply_user_id 为空，对回复回复需要@对方，否则不知道回复谁。
 */
@Entity // 实体
public class Comment implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id // 主键
	@GeneratedValue(strategy = GenerationType.IDENTITY) // 自增长策略
	private Long id; // 唯一标识

	@Column(name = "pid")
	private Long pid = 0L;  //父评论，如果不设置，默认为0

	//若不设置为懒加载会导致blog级联查询一个pid=0的comment对象,此comment对象又级联查询一次其子评论列表，造成blog的comments含有大量重复的comment
	@OneToMany(cascade = {CascadeType.REMOVE},fetch = FetchType.LAZY)  //删除blog时级联删除comment再级联删除其子评论
	@JoinColumn(name = "pid",referencedColumnName = "id")  //pid是comment表增加的指向本表(comment表)id的外键
	@OrderBy("id DESC")   //子评论根据id排序
	private List<Comment> commentList;  //子评论

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reply_user_id")  //一对一时的name是本表中新增的指向另一个表的外键名。
	private User replyUser;  //被回复者

	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(cascade = {CascadeType.MERGE,CascadeType.REMOVE},mappedBy = "comment")  //删除blog时级联删除comment再级联删除其子评论,必须级联合并(若不级联合并vote时commentId=null)，mappBy表示关系被维护端，只有关系端有权去更新外键
	private List<Vote> voteList;

	@NotEmpty(message = "评论内容不能为空")
	@Size(min=2, max=500)
	@Column(nullable = false) // 映射为字段，值不能为空
	private String content;
 
	@OneToOne(cascade = CascadeType.DETACH, fetch = FetchType.EAGER)
	@JoinColumn(name="user_id")
	private User user;

	@ManyToOne
	@JoinColumn(name = "blog_id")
	private Blog blog;

	@Column(nullable = false) // 映射为字段，值不能为空
	@org.hibernate.annotations.CreationTimestamp  // 由数据库自动创建时间
	private Timestamp createTime;

	protected Comment() {
	}
	public Comment(User user, String content) {
		this.content = content;
		this.user = user;
	}
	public Comment(Long pid,User user,User replyUser, String content,Blog blog) {
		this.pid = pid;
		this.user = user;
		this.replyUser = replyUser;
		this.content = content;
		this.blog = blog;
	}

	/**
	 * 若已存在该vote(已被vote中的user赞过则返回true),否则add并返回false
	 */
	public boolean addVote(Vote vote){
		boolean isExist = false;
		for (Vote v : voteList){
			if (v.getUser().getId().equals(vote.getUser().getId())){
				isExist = true;
				break;
			}
		}
		//若不存在次此赞则add进去
		if (!isExist){
			voteList.add(vote);
		}
		return isExist;
	}

	public void removeVote(Long voteId){
		for (Vote v : voteList){
			if (v.getId() == voteId){
				voteList.remove(v);
				break;
			}
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getPid() {
		return pid;
	}

	public void setPid(Long pid) {
		this.pid = pid;
	}

	public List<Comment> getCommentList() {
		return commentList;
	}

	public void setCommentList(List<Comment> commentList) {
		this.commentList = commentList;
	}

	public User getReplyUser() {
		return replyUser;
	}

	public void setReplyUser(User replyUser) {
		this.replyUser = replyUser;
	}

	public List<Vote> getVoteList() {
		return voteList;
	}

	public void setVoteList(List<Vote> voteList) {
		this.voteList = voteList;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	@Override
	public String toString() {
		return "Comment{" +
				"id=" + id +
				", pid=" + pid +
				", commentList=" + commentList +
				", replyUser=" + replyUser +
				", voteList=" + voteList +
				", content='" + content + '\'' +
				", user=" + user +
				", createTime=" + createTime +
				'}';
	}
}
