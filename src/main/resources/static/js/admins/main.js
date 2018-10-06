/**
 * 根据被点击的菜单项替换右侧工作区
 */
//开启js严格模式
"use strict";

// DOM 加载完再执行
$(function() {

    //菜单项点击事件
	$(".blog-menu .list-group-item").click(function () {
		//拿到被点击的菜单项的url
		var url = $(this).attr("url");

        // 先移除全部的点击样式，再添加当前的点击样式
		$(".blog-menu .list-group-item").removeClass("active");
		$(this).addClass("active");

		//加载其他模块的页面到右侧工作区
		$.ajax({
			async:true,
			url:url,
			type:"GET",
			success:function (data) {
				//把Controller返回的数据和html代码块放入右侧工作区容器
				$("#rightContainer").html(data);
            },
			error:function () {
				toastr.error("error");
            }
		})
    });

    //默认选中菜单首项(触发菜单第一项的点击事件),必须先定义点击事件
    $(".blog-menu .list-group-item:first").trigger("click");

});