package com.lee.blogdemo1.entity;

import com.github.rjeschke.txtmark.Processor;
import lombok.Data;
import org.hibernate.annotations.Where;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.elasticsearch.annotations.Document;
import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * Blog 实体
 * 因为需要根据blog查此博文的所有评论，所以此类的comments改用双向一对多关系，而且级联权限是all,所以remove(blog)会级联remove其所有评论
 */
@Entity // 实体
@Data
@Document(indexName = "blog", type = "blog") //indexName索引库的名称,类型(建议以实体的名称命名)
public class Blog implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id // 主键
	@GeneratedValue(strategy = GenerationType.IDENTITY) // 自增长策略
	private Long id; // 唯一标识
	
	@NotEmpty(message = "标题不能为空")
	@Size(min=2, max=50)
	@Column(nullable = false, length = 50) // 映射为字段，值不能为空
	private String title;
	
	@NotEmpty(message = "摘要不能为空")
	@Size(min=2, max=300)
	@Column(nullable = false) // 映射为字段，值不能为空
	private String summary;

	@Lob  // 大对象，映射 MySQL 的 Long Text 类型
	@Basic(fetch= FetchType.LAZY) // 懒加载
	@NotEmpty(message = "内容不能为空")
	@Size(min=2)
	@Column(nullable = false) // 映射为字段，值不能为空
	private String content;
	
	@Lob  // 大对象，映射 MySQL 的 Long Text 类型
	@Basic(fetch= FetchType.LAZY) // 懒加载
	@NotEmpty(message = "内容不能为空")
	@Size(min=2)
	@Column(nullable = false) // 映射为字段，值不能为空
	private String htmlContent; // 将 markdown 转为 html
 	
	@OneToOne(cascade = CascadeType.DETACH, fetch = FetchType.LAZY)
	@JoinColumn(name="user_id")
	private User user;
	
	@Column(nullable = false) // 映射为字段，值不能为空
	@org.hibernate.annotations.CreationTimestamp  // 由数据库自动创建时间
	private Timestamp createTime;

	@Column(name="readSize")
	private Integer readSize = 0; // 访问量、阅读量
	 
	@Column(name="commentSize")
	private Integer commentSize = 0;  // 评论量

	@Column(name="voteSize")
	private Integer voteSize = 0;  // 点赞量

	private String blogFileId;  //文件服务器标记文件所属博文的id,在创建blog对象时生成



	//中间表名称:blog_comment
//	@JoinTable(name = "blog_comment", joinColumns = @JoinColumn(name = "blog_id", referencedColumnName = "id"), //blog表加到中间表的外键
//		inverseJoinColumns = @JoinColumn(name = "comment_id", referencedColumnName = "id"))  //另一个表加到中间表的外键
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER,mappedBy = "blog")   //所有级联操作权限(删除文章时会删除其所有评论),急加载：查询blog时候马上查询comments
//	@JoinColumn(name = "blog_id",referencedColumnName = "id")  //blog_id是comment表新增的指向本表(blog表)id的外键
	@Where(clause = "pid=0") //因为我们在blog实体中关联查询评论列表，只查询一级评论即可，二级评论(pid!=0)由 Comment 实体中的 CommentList 自关联查询。
	@OrderBy("create_time DESC")  //最新排序
	private List<Comment> comments;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "blog_vote", joinColumns = @JoinColumn(name = "blog_id", referencedColumnName = "id"),
		inverseJoinColumns = @JoinColumn(name = "vote_id", referencedColumnName = "id"))
	private List<Vote> votes;  //所有点赞对象列表，点赞对象里又记录了点赞用户

	@OneToOne(cascade = CascadeType.DETACH, fetch = FetchType.LAZY)
	@JoinColumn(name="category_id")
	private Category category;

	@Column(name="tags", length = 100)
	private String tags;  // 标签
	
	protected Blog() {
		// TODO Auto-generated constructor stub
	}
	public Blog(String title, String summary,String content,String blogFileId) {
		this.title = title;
		this.summary = summary;
		this.content = content;
		this.blogFileId = blogFileId;
	}
	

	public void setContent(String content) {
		this.content = content;
		this.htmlContent = Processor.process(content);  //将markdown内容转为html格式
	}
	public void setComments(List<Comment> comments) {
		this.comments = comments;
		this.commentSize = this.comments.size();
	}
 
	/**
	 * 添加评论,同时更新评论量
	 */
	public void addComment(Comment comment) {
		this.comments.add(comment);
		//更新当前评论列表大小
		this.commentSize = this.comments.size();
	}
	/**
	 * 删除评论,同时更新评论量
	 */
	public void removeComment(Long commentId) {
		for (int i=0; i < this.comments.size(); i ++ ) {
			if (comments.get(i).getId() == commentId) {
				this.comments.remove(i);
				break;
			}
		}
		//更新当前评论列表大小
		this.commentSize = this.comments.size();
	}
 
	/**
	 * 增加传入的用户点赞
	 */
	public boolean addVote(Vote vote) {
		boolean isExist = false;
		// 遍历所有点赞判断重复
		for (int i=0; i < this.votes.size(); i ++ ) {
			if (this.votes.get(i).getUser().getId() == vote.getUser().getId()) {
				isExist = true;
				break;
			}
		}
		
		if (!isExist) {
			this.votes.add(vote);
			this.voteSize = this.votes.size();
		}

		return isExist;
	}
	/**
	 * 删除传入的用户点赞 (取消点赞)
	 */
	public void removeVote(Long voteId) {
		//遍历所有点赞移除此voteId的点赞
		for (int i=0; i < this.votes.size(); i ++ ) {
			if (this.votes.get(i).getId() == voteId) {
				this.votes.remove(i);
				break;
			}
		}
		this.voteSize = this.votes.size();
	}
	public void setVotes(List<Vote> votes) {
		this.votes = votes;
		this.voteSize = this.votes.size();
	}

}
