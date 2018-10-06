/*!
 * 分页处理.
 * 
 * @since: 1.0.0
 * @author Way Lau <https://waylau.com>
 */
(function($) {

    "use strict";

    /**
     * 定义绑定事件
     * handler:pageIndex 所选页面的索引，从0开始；pageSize 页面的大小，这里默认是10。
     */
    $.tbpage = function(selector, handler) {

        //先清除page.html中的分页按钮的所有点击(click)事件,再重新绑定
        $(selector).off("click", ".tbpage-item").on("click", ".tbpage-item", function() {

            var pageIndex = $(this).attr("pageIndex");

            var pageSize = $('.tbpage-size option:selected').val();
            // 判断所选元素是否为当前页面
            if($(this).parent().attr("class").indexOf("active")>0){  //若是当前页无需处理
                //console.log("为当前页面");
            }else{  //若不是当前页面则调用getUersByName(pageIndex, pageSize)发ajax请求翻页
                handler(pageIndex, pageSize);
            }
        });


        //先清除page.html中的页面大小的所有改变(change)事件,再重新绑定
        $(selector).off("change", ".tbpage-size").on("change", ".tbpage-size", function() {

            var pageIndex = $(this).attr("pageIndex");
			//获取当前选择的size
            var pageSize = $('.tbpage-size option:selected').val();

            //发ajax请求页面
            handler(pageIndex, pageSize);
        });
    };

})(jQuery);