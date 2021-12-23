package com.cym.controller;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.ModelAndView;

import com.cym.ext.GroupExt;
import com.cym.ext.Select;
import com.cym.model.Group;
import com.cym.model.User;
import com.cym.service.ConfigService;
import com.cym.service.GroupService;
import com.cym.sqlhelper.bean.Page;
import com.cym.utils.BaseController;
import com.cym.utils.BeanExtUtil;
import com.cym.utils.JsonResult;

import cn.hutool.core.io.FileUtil;

@Controller
@Mapping("/adminPage/group")
public class GroupController extends BaseController {
	@Inject
	GroupService groupService;
	@Inject
	ConfigService configService;

	@Mapping("")
	public ModelAndView index(Page page, String keywords) {

		page = groupService.search(page, keywords);
		Page pageExt = BeanExtUtil.copyPageByProperties(page, GroupExt.class);
		for (GroupExt groupExt : (List<GroupExt>) pageExt.getRecords()) {
			groupExt.setUserList(groupService.getUserList(groupExt.getId()));
		}

		ModelAndView modelAndView = buildMav("/adminPage/group/index.html");
		
		modelAndView.put("keywords", keywords);
		modelAndView.put("page", pageExt);


		return modelAndView;
	}

	
	@Mapping("addOver")
	public JsonResult addOver(Group group,  String[] userIds) {
		Group groupOrg = groupService.getByName(group.getName(), group.getId());
		if (groupOrg != null) {
			return renderError("此小组名已存在");
		}

		groupService.insertOrUpdate(group, userIds);
		configService.refresh();
		return renderSuccess();
	}

	@Mapping("detail")
	public JsonResult detail(String id) {
		Group group = sqlHelper.findById(id, Group.class);
		GroupExt groupExt = BeanExtUtil.copyNewByProperties(group, GroupExt.class);
		List<User> userList = groupService.getUserList(group.getId());
		groupExt.setUserList(userList);

		groupExt.setUserIds(userList.stream().map(User::getId).collect(Collectors.toList()));
		
		return renderSuccess(groupExt);
	}

	
	@Mapping("del")
	
	public JsonResult del(String id) {
		groupService.deleteById(id);
		configService.refresh();
		return renderSuccess();
	}

	
	@Mapping("importOver")
	
	public JsonResult importOver(String dirTemp) {

		List<String> lines = FileUtil.readLines(dirTemp, Charset.forName("UTF-8"));
		boolean start = false;
		for (String line : lines) {
			if (line.contains("[groups]")) {
				start = true;
			}
			if (start && line.contains("[") && line.contains("]") && !line.contains("[groups]")) {
				start = false;
			}

			if (start && line.contains("=")) {
				String name = line.split("=")[0].trim();
				String users = line.split("=")[1].trim();

				groupService.importGroup(name, users);
			}
		}

		FileUtil.del(dirTemp);

		return renderSuccess();
	}

	@Mapping("getUserList")
	
	public JsonResult getUserList() {

		List<User> users = sqlHelper.findAll(User.class);

		List<Select> selects = new ArrayList<>();
		for (User user : users) {
			Select select = new Select();
			select.setName(user.getTrueName());
			select.setValue(user.getId());
			selects.add(select);
		}

		return renderSuccess(selects);
	}
}
