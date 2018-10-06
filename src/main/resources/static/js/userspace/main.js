/*!
 * Avatar JS.
 * 
 * @since: 1.0.0 2017/4/6
 * @author Way Lau <https://waylau.com>
 */
"use strict";
//# sourceURL=main.js
 
// DOM 加载完再执行
$(function() {
	var avatarApi;
	
	// 获取编辑用户头像的界面,见profile.html
	$(".blog-content-container").on("click",".blog-edit-avatar", function () { 
		avatarApi = "/u/"+$(this).attr("userName")+"/avatar";
		$.ajax({ 
			 url: avatarApi, 
			 success: function(data){
			 	//返回的视图数据放入profile.html的avatarFormContainer中
				 $("#avatarFormContainer").html(data);
		     },
		     error : function() {
		    	 toastr.error("error!");
		     }
		 });
	});
	
	/**
	 * <img>输入是base64编码
	 * 将以base64的图片url数据转换为Blob  
	 * 用url方式表示的base64图片数据
	 */  
	function convertBase64UrlToBlob(urlData){  
	      
	    var bytes=window.atob(urlData.split(',')[1]); //去掉url的头，并转换为byte  
	      
	    //处理异常,将ascii码小于0的转换为大于0  
	    var ab = new ArrayBuffer(bytes.length);  
	    var ia = new Uint8Array(ab);  
	    for (var i = 0; i < bytes.length; i++) {  
	        ia[i] = bytes.charCodeAt(i);  
	    }  
	  
	    return new Blob( [ab] , {type : 'image/png'});  
	} 
	
	// 提交用户头像的图片数据
	$("#submitEditAvatar").on("click", function () { 
		var form = $('#avatarformid')[0];  
	    var formData = new FormData(form);
	    //cropImg>img 是cropImg类标签下的<img>
	    var base64Codes = $(".cropImg > img").attr("src");
	    //转码后放入表单中提交
 	    formData.append("file",convertBase64UrlToBlob(base64Codes));
		formData.append("userId",$("#userId").val());
		formData.append("isHeadImg",1);  //文件服务器端用户头像文件的标识


 	    //请求文件服务器保存新头像
 	    $.ajax({
		    url: fileServerUrl,  // 文件服务器上传文件地址,在profile.html中的js已声明
		    type: 'POST',
		    cache: false,
		    data: formData,
		    processData: false,
		    contentType: false,
		    success: function(data){
		    	//文件服务器后台返回的新的头像url
		    	var avatarUrl = data;
				// 获取header.html的 CSRF Token
				var csrfToken = $("meta[name='_csrf']").attr("content");
				var csrfHeader = $("meta[name='_csrf_header']").attr("content");

                // 头像已成功上传文件服务器，更新数据库的头像url
                $.ajax({
                    url: avatarApi,
                    type: 'POST',
                    //contentType: "application/json; charset=utf-8",
					 //若要这2个请求参数直接赋值到JavaBean(user)中的对应属性中，则要使用JSON.stringify({'':''})
					 data: {"userId":$("#userId").val(), "avatarUrl":avatarUrl},
					 //发送前在请求头添加  CSRF Token 否则ajax请求会失败
					 beforeSend: function(request) {
		                 request.setRequestHeader(csrfHeader, csrfToken);
		             },
					 success: function(data){
						 if (data.success) {
							 // 成功后，置换头像图片
							 $(".blog-avatar").attr("src", data.avatarUrl);
							 //刷新页面(更新头像显示)
                             location.reload();
						 } else {
							 toastr.error("error!"+data.message);
						 }
				     },
				     error : function() {
				    	 toastr.error("error!");
				     }
				 });
	        },
		    error : function() {
		    	 toastr.error("error!");
		    }
		})
	});
});