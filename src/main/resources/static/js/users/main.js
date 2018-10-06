/*!
 * Bolg main JS.
 * 
 * @since: 1.0.0 2017/3/9
 * @author Way Lau <https://waylau.com>
 */
"use strict";
//# sourceURL=main.js

//DOM 加载完再执行
$(function() {

    var _pageSize; // 存储用于搜索

    // 用于点击底部分页栏时根据用户名、页面索引、页面大小搜索用户列表
    function getUersByName(pageIndex, pageSize) {
        $.ajax({
            url: "/users",
            contentType : 'application/json',
            data:{
                "async":true,  //异步调用 ,默认值: true
                "pageIndex":pageIndex,
                "pageSize":pageSize,
                "name":$("#searchName").val()
            },
            success: function(data){
                //后台返回的数据设置列表容器的内容
                $("#mainContainer").html(data);
            },
            error : function() {
                //toastr 通知提示插件
                toastr.error("error!");
            }
        });
    }

    // 为id=mainContainer的容器下引入的分页组件绑定处理器分页事件
    $.tbpage("#mainContainer", function (pageIndex, pageSize) {
        getUersByName(pageIndex, pageSize);
        //保存翻页后的pageSize
        _pageSize = pageSize;
    });

    // 搜索
    $("#searchNameBtn").click(function() {
        getUersByName(0, _pageSize);
    });

    // 获取添加用户的界面
    $("#addUser").click(function() {
        $.ajax({
            url: "/users/add",
            success: function(data){
                $("#userFormContainer").html(data);
            },
            error : function() {
                toastr.error("error!");
            }
        });
    });

    // 获取编辑用户的界面
    $("#rightContainer").on("click",".blog-edit-user", function () {
        $.ajax({
            url: "/users/edit/" + $(this).attr("userId"),
            success: function(data){
                $("#userFormContainer").html(data);
            },
            error : function() {
                toastr.error("error");
            }
        });
    });

    // 保存或修改，提交变更后，清空表单
    $("#submitEdit").click(function() {
        $.ajax({
            url: "/users",
            type: 'POST',
            data:$('#userForm').serialize(),
            success: function(data){
                $('#userForm')[0].reset();
                //统一使用自定义Response对象中的success域判断是否成功
                if (data.success) {
                    // 从新刷新主界面
                    getUersByName(0, _pageSize);
                } else {
                    toastr.error(data.message);
                }
            },
            error : function() {  //请求错误状态码时调用
                toastr.error("error");
            }
        });
    });

    // 删除用户
    $("#rightContainer").on("click",".blog-delete-user", function () {
        // 获取 CSRF Token
        var csrfToken = $("meta[name='_csrf']").attr("content");
        var csrfHeader = $("meta[name='_csrf_header']").attr("content");
        $.ajax({
            url: "/users/" + $(this).attr("userId") ,
            type: 'DELETE',
            beforeSend: function(request) {
                request.setRequestHeader(csrfHeader, csrfToken); // 添加  CSRF Token
            },
            success: function(data){
                if (data.success) {
                    toastr.success(data.message);
                    // 刷新主界面
                    getUersByName(0, _pageSize);
                } else {
                    toastr.error(data.message);
                }
            },
            error : function(data) {
                toastr.error(data.message);
            }
        });
    });
});