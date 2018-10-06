/*!
 * blogedit.html 页面脚本.
 * 
 * @since: 1.0.0 2017-03-26
 * @author Way Lau <https://waylau.com>
 */
"use strict";
//# sourceURL=blogedit.js

// DOM 加载完再执行
$(function() {
	
	// 初始化 md 编辑器
    $("#md").markdown({
        language: 'zh',
        fullscreen: {
            enable: true
        },
        resize:'vertical',
        localStorage:'md'
    });
        
    $('.form-control-chosen').chosen();
    
    //博文编辑页的插入图片按钮
    $("#uploadImage").click(function() {
        var formData = new FormData($('#uploadformid')[0]);
        formData.append("blogFileId",blogFileId);
        $.ajax({
            url: fileServerUrl,
            type: 'POST',
            cache: false,
            data: formData,
            processData: false,
            contentType: false,
            success: function(data){
                var mdcontent=$("#md").val();
                 $("#md").val(mdcontent + "\n![]("+data +") \n");

             }
        }).done(function(res) {
            $('#file').val('');
        }).fail(function(res) {});
    });

    // 发布博客
    $("#submitBlog").click(function() {

        // 获取 CSRF Token 
        var csrfToken = $("meta[name='_csrf']").attr("content");
        var csrfHeader = $("meta[name='_csrf_header']").attr("content");

        $.ajax({
            url: '/u/'+ $(this).attr("userName") + '/blogs/edit',
            type: 'POST',
            contentType: "application/json; charset=utf-8",
            data:JSON.stringify({"id":$('#blogId').val(),
                                "blogFileId":$("#blogFileId").val(),
                                "title": $('#title').val(),
                                "summary": $('#summary').val() ,
                                "content": $('#md').val(),
                                "category":{"id":$('#categorySelect').val()},  //注入category对象的id属性
                                "tags":$('.form-control-tag').val()
                                }),
            beforeSend: function(request) {
                request.setRequestHeader(csrfHeader, csrfToken); // 添加  CSRF Token 
            },
             success: function(data){
                 if (data.success) {
                    // 成功后，重定向
                     window.location = data.body;
                 } else {
                     toastr.error("error!"+data.message);
                 }

             },
             error : function() {
                 toastr.error("error!");
             }
        })
    })
    
    // 初始化标签
    $('.form-control-tag').tagsInput({
    	'defaultText':'输入标签'
    });
});