/*!
 * u main JS.
 * 
 * @since: 1.0.0 2017/3/9
 * @author Way Lau <https://waylau.com>
 */
"use strict";
//# sourceURL=u.js

// DOM 加载完再执行
$(function() {

    //用户博客列表页加载完,获取分类列表
    getCategories(username); //username是url上的博主
	 
	var _pageSize; // 存储用于搜索
	
	// 根据用户名、页面索引、页面大小获取用户列表
	function getBlogsByName(pageIndex, pageSize) {
		 $.ajax({ 
			 url: "/u/"+  username  +"/blogs", 
			 contentType : 'application/json',
			 data:{
				 "async":true, 
				 "pageIndex":pageIndex,
				 "pageSize":pageSize,
				 "category": categoryId,
				 "keyword":$("#keyword").val()
			 },
			 success: function(data){
				 $("#mainContainer").html(data);
				 
				 // 如果是分类查询，则取消最新、最热选中样式
				 if (categoryId) {
					$(".nav-item .nav-link").removeClass("active");
				 }
		     },
		     error : function() {
		    	 toastr.error("error!");
		     }
		 });
	}
	
	// 分页
	$.tbpage("#mainContainer", function (pageIndex, pageSize) {
		getBlogsByName(pageIndex, pageSize);
		_pageSize = pageSize;
	});
   
	// 关键字搜索
	$("#searchBlogs").click(function() {
		getBlogsByName(0, _pageSize);
	});
	
	// 最新\最热切换事件
	$(".nav-item .nav-link").click(function() {
 
		var url = $(this).attr("url");
		
		// 先移除其他的点击样式，再添加当前的点击样式
		$(".nav-item .nav-link").removeClass("active");
		$(this).addClass("active");  
 
		// 加载其他模块的页面到右侧工作区
		 $.ajax({ 
			 url: url+'&async=true', 
			 success: function(data){
				 $("#mainContainer").html(data);
			 },
			 error : function() {
				 toastr.error("error!");
			 }
		 });
		 
		 // 清空搜索框内容
		 $("#keyword").val('');
	});
	
	var categoryId;
	
	// 获取分类列表
	function getCategories(username) {
		// 获取 CSRF Token 
	 
		$.ajax({ 
			 url: '/categories',
			 type: 'GET',
			 data:{"username":username},
			 success: function(data){
				$("#categoryMain").html(data);
		     },
		     error : function() {
		    	 toastr.error("error!");
		     }
		 });
	}
		
	// 获取编辑分类的页面
	$(".blog-content-container").on("click",".blog-add-category", function () {
		$.ajax({ 
			 url: '/categories/edit',
			 type: 'GET', 
			 success: function(data){
				 $("#categoryFormContainer").html(data);
		     },
		     error : function() {
		    	 toastr.error("error!");
		     }
		 });
	});
		
	// 获取编辑某个分类的页面
	$(".blog-content-container").on("click",".blog-edit-category", function () {
	 
		$.ajax({ 
			 url: '/categories/edit/'+$(this).attr('categoryId'),
			 type: 'GET', 
			 success: function(data){
				 $("#categoryFormContainer").html(data);
		     },
		     error : function() {
		    	 toastr.error("error!");
		     }
		 });
	});
		

	// 提交分类
	$("#submitEditCategory").click(function() {
		// 获取 CSRF Token
		var csrfToken = $("meta[name='_csrf']").attr("content");
		var csrfHeader = $("meta[name='_csrf_header']").attr("content");

		$.ajax({
			url : '/categories',
			type : 'POST',
			contentType : "application/json; charset=utf-8",
			data : JSON.stringify({
				"username" : username,
				"category" : {
					"id" : $('#categoryId').val(),
					"name" : $('#categoryName').val()
				}
			}),
			beforeSend : function(request) {
				request.setRequestHeader(csrfHeader, csrfToken); // 添加 CSRF Token
			},
			success : function(data) {
				if (data.success) {
					toastr.info(data.message);
					// 成功后，刷新列表
					getCategories(username);
				} else {
					toastr.error(data.message);
				}
			},
			error : function() {
				toastr.error("error!");
			}
		});
	});
		
	// 删除分类
	$(".blog-content-container").on("click",".blog-delete-category", function () {
		// 获取 CSRF Token 
		var csrfToken = $("meta[name='_csrf']").attr("content");
		var csrfHeader = $("meta[name='_csrf_header']").attr("content");
		
		$.ajax({ 
			 url: '/categories/'+$(this).attr('categoryid')+'?username='+username,
			 type: 'DELETE', 
			 beforeSend: function(request) {
	             request.setRequestHeader(csrfHeader, csrfToken); // 添加  CSRF Token 
	         },
			 success: function(data){
				 if (data.success) {
					 toastr.info(data.message);
					 // 成功后，刷新列表
					 getCategories(username);
				 } else {
					 toastr.error(data.message);
				 }
		     },
		     error : function() {
		    	 toastr.error("error!");
		     }
		 });
	});
		
	// 根据分类查询按钮
	$(".blog-content-container").on("click",".blog-query-by-category", function () {
        categoryId = $(this).attr('categoryId');
		getBlogsByName(0, _pageSize);
	});

});