/*!
 * blog.html 页面脚本.
 * 
 * @since: 1.0.0 2017-03-26
 * @author Way Lau <https://waylau.com>
 */
"use strict";
//# sourceURL=blog.js

// DOM 加载完再执行
// $(function() {
	$.category("#category", ".post-content");
	
	// 点击删除按钮,处理删除博客事件
	$(".blog-content-container").on("click",".blog-delete-blog", function () { 
	    // 获取 CSRF Token 
	    var csrfToken = $("meta[name='_csrf']").attr("content");
	    var csrfHeader = $("meta[name='_csrf_header']").attr("content");

	    $.ajax({
	         url: blogUrl, 
	         type: 'DELETE', 
	         beforeSend: function(request) {
	             request.setRequestHeader(csrfHeader, csrfToken); // 添加  CSRF Token 
	         },
	         success: function(data){
	             if (data.success) { //删除成功后
	                 // 返回的body是重定向的url
					 window.location = data.body;
	                 //发ajax请求文件服务器删除此博文中插入的图片
					 $.ajax({
						 url:fileServerDeleteUrl +"/"+ blogFileId,
						 type:"DELETE"
					 });
	             } else {
	                 toastr.error(data.message);
	             }
	         },
	         error : function() {
	             toastr.error("error!");
	         }
	     });
	});
	
	// 获取评论列表
	function getCommnet(blogId) {
		//显示评论加载中提示
        $('#loading').show();

		$.ajax({
			 url: '/comments',
			 type: 'GET',
			 data:{"blogId":blogId},
			 success: function(data){
                 //隐藏评论加载中提示
                 $('#loading').hide();

				 $("#mainContainer").html(data);
		     },
		     error : function() {
                 //隐藏评论加载中提示
                 $('#loading').hide();
		    	 toastr.error("error!");
		     }
		 });
	}

	// 博客提交一级评论,pid=0
	$(".blog-content-container").on("click","#submitComment", function () { 
		// 获取 CSRF Token 
		var csrfToken = $("meta[name='_csrf']").attr("content");
		var csrfHeader = $("meta[name='_csrf_header']").attr("content");
		
		$.ajax({ 
			 url: '/comments',
			 type: 'POST', 
			 data:{"blogId":blogId, "commentContent":$('#commentContent').val()},
			 beforeSend: function(request) {
	             request.setRequestHeader(csrfHeader, csrfToken); // 添加  CSRF Token 
	         },
			 success: function(data){
				 if (data.success) {
					 // 清空评论框
					 $('#commentContent').val('');
					 // 获取评论列表
					 getCommnet(blogId);
				 } else {
					 toastr.error(data.message);
				 }
		     },
		     error : function(data) {
                 toastr.error(data.message);
		     }
		 });
	});

	// 删除评论
	$(".blog-content-container").on("click",".blog-delete-comment", function () { 
		// 获取 CSRF Token 
		var csrfToken = $("meta[name='_csrf']").attr("content");
		var csrfHeader = $("meta[name='_csrf_header']").attr("content");
		
		$.ajax({ 
			 url: '/comments/'+$(this).attr("commentId")+'?blogId='+blogId, 
			 type: 'DELETE', 
			 beforeSend: function(request) {
	             request.setRequestHeader(csrfHeader, csrfToken); // 添加  CSRF Token 
	         },
			 success: function(data){
				 if (data.success) {
					 // 获取评论列表
					 getCommnet(blogId);
				 } else {
					 toastr.error(data.message);
				 }
		     },
		     error : function() {
		    	 toastr.error("error!");
		     }
		 });
	});

	// 初始化 博客评论
	getCommnet(blogId);
	
	// 提交点赞
	$(".blog-content-container").on("click","#submitVote", function () { 
		// 获取 CSRF Token 
		var csrfToken = $("meta[name='_csrf']").attr("content");
		var csrfHeader = $("meta[name='_csrf_header']").attr("content");
		
		$.ajax({ 
			 url: '/votes',
			 type: 'POST', 
			 data:{"blogId":blogId},
			 beforeSend: function(request) {
	             request.setRequestHeader(csrfHeader, csrfToken); // 添加  CSRF Token 
	         },
			 success: function(data){
				 if (data.success) {
					 toastr.info(data.message);
						// 成功后，重定向
					 window.location = blogUrl;
				 } else {
					 toastr.error(data.message);
				 }
		     },
		     error : function() {
		    	 toastr.error("error!");
		     }
		 });
	});

	// 取消点赞
	$(".blog-content-container").on("click","#cancelVote", function () { 
		// 获取 CSRF Token 
		var csrfToken = $("meta[name='_csrf']").attr("content");
		var csrfHeader = $("meta[name='_csrf_header']").attr("content");
		
		$.ajax({ 
			 url: '/votes/'+$(this).attr('voteId')+'?blogId='+blogId,
			 type: 'DELETE', 
			 beforeSend: function(request) {
	             request.setRequestHeader(csrfHeader, csrfToken); // 添加  CSRF Token 
	         },
			 success: function(data){
				 if (data.success) {
					 toastr.info(data.message);
					// 成功后，重定向
					 window.location = blogUrl;
				 } else {
					 toastr.error(data.message);
				 }
		     },
		     error : function() {
		    	 toastr.error("error!");
		     }
		 });
	});
// });

//=============================================多级评论：edited by Lee34========================================
//点击每条评论的回复按钮事件
function replyComment(target) {
    //点击回复评论按钮时先清空文本再弹出模态框
    $("#reply_commentContent").val("");
    $("#reply-comment-modal").modal("show");

    //1.保存父评论的commentId到模态框中的pid
    var commentId = $(target).parents(".commment-Item").attr("commentId");
    $("#reply_comment_form").attr("pid",commentId);
    //2.此评论的评论者是被回复者,保存到模态框
    var replyUserId = $(target).attr("userId");
    $("#reply_comment_form").attr("replyUserId",replyUserId);
    var replyUserName = $(target).attr("userName");
    //3.更改文本的placeholder
    $("#reply_commentContent").attr("placeholder","@"+replyUserName+":");
    //4.是否回复一级评论(回复一级评论replyUser=null)
    $("#reply_comment_form").attr("isReplyComment",$(target).attr("isReplyComment"));
}


//点击模态框的回复评论按钮事件
function reply(target) {
    // 获取 CSRF Token
    var csrfToken = $("meta[name='_csrf']").attr("content");
    var csrfHeader = $("meta[name='_csrf_header']").attr("content");

    //获取父评论id,评论者id,被回复者id
    var pid = $("#reply_comment_form").attr("pid");
    var replyUserId = $("#reply_comment_form").attr("replyUserId");
    var isReplyComment = $("#reply_comment_form").attr("isReplyComment");
    var data;
    //回复二级评论需要传replyUser
    if (isReplyComment == "true"){
        data = {"blogId":blogId,"pid":pid,"replyUserId":replyUserId,"commentContent":$("#reply_commentContent").val()};
	}else {
        //回复一级评论不需要传replyUser
        data = {"blogId":blogId,"pid":pid,"commentContent":$("#reply_commentContent").val()};
	}

    //ajax请求保存评论
    $.ajax({
        url: '/comments',
        type: 'POST',
        data: data,
        beforeSend:function(request) {
            request.setRequestHeader(csrfHeader,csrfToken); // 添加  CSRF Token
        },
        success:function(data) {
            if (data.success){
                //重新加载评论
                location.reload();
            }else {
                toastr.error(data.message);
            }
        },
        error:function(data) {
            toastr.error(data.message);
        }
    });
}


//父子评论点赞或取消按钮事件
function voteOrcancelComment(target) {
    // 获取 CSRF Token
    var csrfToken = $("meta[name='_csrf']").attr("content");
    var csrfHeader = $("meta[name='_csrf_header']").attr("content");

    //若是点赞按钮
    if ($(target).children('span').hasClass('vote')){
        $.ajax({
            url:"/votes/comment",
            type:"POST",
            data:{"commentId":$(target).attr('commentId')},
            beforeSend:function(request) {
                request.setRequestHeader(csrfHeader,csrfToken);
            },
            success:function(data) {
                if (data.success){
                    toastr.success("点赞成功");
                    getCommnet(blogId);
                }else {
                    toastr.error(data.message);
                }
            },
            error:function(data) {
                toastr.error(data.message);
            }
        });
    }else {//取消按钮
        $.ajax({
            url:"/votes/comment/"+"?commentId="+$(target).attr('commentId'), //只需要根据评论者和评论id就可以取消点赞
            type:"DELETE",
            beforeSend:function(request) {
                request.setRequestHeader(csrfHeader,csrfToken);
            },
            success:function(data) {
                if (data.success){
                    toastr.success(data.message);
                    getCommnet(blogId);
                }else {
                    toastr.error(data.message);
                }
            },
            error:function(data) {
                toastr.error(data.message);
            }
        });
    }
}