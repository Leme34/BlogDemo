package com.lee.blogdemo1.controller.admin;

import com.lee.blogdemo1.VO.Menu;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户控制器.
 * 
 * @author <a href="https://waylau.com">Way Lau</a>
 * @date 2017年2月26日
 */
@Controller
@RequestMapping("/admins")
public class AdminController {

	/**
	 * 获取后台管理主页面菜单
	 */
	@GetMapping
	public ModelAndView listUsers(Model model) {
		//菜单列表
		List<Menu> menuList = new ArrayList<>();
		//新增一个"用户管理"菜单
		menuList.add(new Menu("用户管理","/users"));

		model.addAttribute("list", menuList);
		//跳转后台管理主页面
		return new ModelAndView("admins/index", "model", model);
	}
 
	 
}
