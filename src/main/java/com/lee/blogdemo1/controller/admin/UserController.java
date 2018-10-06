package com.lee.blogdemo1.controller.admin;

import com.lee.blogdemo1.VO.Response;
import com.lee.blogdemo1.entity.Authority;
import com.lee.blogdemo1.entity.User;
import com.lee.blogdemo1.service.AuthorityService;
import com.lee.blogdemo1.service.CategoryService;
import com.lee.blogdemo1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

/**
 * 提供给/admins管理页面的用户管理接口
 */
@RestController
@RequestMapping("/users")
public class UserController {
 
	@Autowired
	private UserService userService;
	@Autowired
	private AuthorityService authorityService;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private CategoryService categoryService;

	/**
	 * 用户管理页面的共用接口
	 * 发异步请求处：
	 * static/js/users/main.js 用户管理页面分页搜索用户
	 * static/js/admins/main.js 获取用户管理页面右边工作区
	 */
	@GetMapping
	public ModelAndView list(@RequestParam(value = "async", required = false) boolean async,
							 @RequestParam(value = "pageIndex", required = false, defaultValue = "0") int pageIndex,
							 @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
							 @RequestParam(value = "name", required = false, defaultValue = "") String name,
							 Model model) {
		//1.分页模糊查询
		Pageable pageable = new PageRequest(pageIndex, pageSize);
		Page<User> userPage = userService.listUsersByNameLike(name, pageable);
		List<User> userList = userPage.getContent();

		//2.数据放入请求域
		model.addAttribute("page", userPage);
		model.addAttribute("userList", userList);

		//3.若ajax异步请求则视图只返回id=mainContainerRepleace的容器(html代码块)，否则跳转到list.html
		return new ModelAndView(async==true?"users/list :: #mainContainerRepleace":"users/list",
				"userModel",model);   //modelName即数据在请求域中的名称
	}

	/**
	 * 跳转创建用户表单页面
	 */
	@GetMapping("/add")
	public ModelAndView createForm(Model model) {
		//传入一个空的user对象???
		model.addAttribute("user", new User(null, null, null, null));
		//跳转
		return new ModelAndView("users/add","userModel",model);
	}

	/**
	 * 保存或者修改用户接口 (提供给/admins管理页面的接口)
	 * @Validated User user: 若数据校验有异常由GlobalExceptionHandler拦截并处理,不执行以下方法
	 */
	@PostMapping
	public ResponseEntity<Response> saveOrUpateUser(@Validated User user, Long authorityId) {  //与前端同名，自动注入
		//更新权限
		List<Authority> authorityList = new ArrayList<>();
		authorityList.add(authorityService.getAuthorityById(authorityId));
		user.setAuthorities(authorityList);

		String encodePwd = passwordEncoder.encode(user.getPassword());//编码后的新密码
		//Update时不能直接编码保存否则会编码2次密码 若当前密码与数据库密码不匹配则更新密码，否则不更新
		if(user.getId()!=null) {
			String oldPwd = userService.getUserById(user.getId()).get().getPassword();//旧密码(已编码)
			if (!oldPwd.equals(user.getPassword())) { //若密码被修改,保存编码后的新密码
				user.setPassword(encodePwd);
			}
		}else {  //新增时密码编码再保存
			user.setPassword(encodePwd);
		}

		//没有异常被拦截则成功
		userService.saveOrUpateUser(user);
//		}catch (Exception e){
//			System.out.println("异常如下：");
//			e.printStackTrace();
//			return ResponseEntity.ok().body(new Response(false, e.getMessage(), user));
//		}
		return ResponseEntity.ok().body(new Response(true, "处理成功", user));
	}

	/**
	 * 删除用户
	 */
	@DeleteMapping(value = "/{id}")
	public ResponseEntity<Response> delete(@PathVariable("id") Long id) {
		//category与user没有关联，所以要先删除此用户的所有分类
		categoryService.removeAllByUser(userService.getUserById(id).get());
		userService.removeUser(id);
		return ResponseEntity.ok().body(new Response(true,"删除成功"));
	}

	/**
	 * 获取修改用户的界面,查询此id的用户信息放入待修改表单中
	 */
	@GetMapping(value = "edit/{id}")
	public ModelAndView modifyForm(@PathVariable("id") Long id, Model model) {
		User user = userService.getUserById(id).get();
		model.addAttribute("user", user);
		return new ModelAndView("users/edit", "userModel", model);
	}


}
