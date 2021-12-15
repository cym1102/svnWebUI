package com.cym.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.cym.config.SqlConfig;
import com.cym.ext.Path;
import com.cym.ext.RepositoryExt;
import com.cym.ext.RepositoryGroupExt;
import com.cym.ext.RepositoryUserExt;
import com.cym.ext.Select;
import com.cym.model.Group;
import com.cym.model.Repository;
import com.cym.model.RepositoryGroup;
import com.cym.model.RepositoryUser;
import com.cym.model.User;
import com.cym.service.ConfigService;
import com.cym.service.RepositoryService;
import com.cym.service.SettingService;
import com.cym.utils.BaseController;
import com.cym.utils.BeanExtUtil;
import com.cym.utils.JarUtil;
import com.cym.utils.JsonResult;
import com.cym.utils.PathUtls;
import com.cym.utils.SystemTool;

import cn.craccd.sqlHelper.bean.Page;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;

@Controller
@RequestMapping("/adminPage/repository")
public class RepositoryController extends BaseController {
	@Autowired
	SqlConfig sqlConfig;
	@Autowired
	RepositoryService repositoryService;
	@Autowired
	ConfigService configService;
	@Autowired
	SettingService settingService;
	@Autowired
	PathUtls pathUtls;

	@RequestMapping("")
	public ModelAndView index(HttpServletRequest request, ModelAndView modelAndView, Page page, String keywords) {
		String port = settingService.get("port");

		page = repositoryService.search(page, keywords);

		Page<RepositoryExt> pageExt = BeanExtUtil.copyPageByProperties(page, RepositoryExt.class);
		for (RepositoryExt repositoryExt : (List<RepositoryExt>) pageExt.getRecords()) {
			String url = "svn://" + getIP(request.getRequestURL().toString() + "/");
			if (!port.equals("3690")) {
				url += (":" + port);
			}
			url += "/" + repositoryExt.getName() + "/";
			repositoryExt.setUrl(url);
		}

		modelAndView.addObject("keywords", keywords);
		modelAndView.addObject("page", pageExt);
		modelAndView.setViewName("/adminPage/repository/index");
		return modelAndView;
	}

	@Transactional
	@RequestMapping("addOver")
	@ResponseBody
	public JsonResult addOver(Repository repository) {
		Repository repositoryOrg = repositoryService.getByName(repository.getName(), repository.getId());
		if (repositoryOrg != null) {
			return renderError("此仓库名已存在");
		}

		repositoryService.insertOrUpdate(repository);
		configService.refresh();
		return renderSuccess();
	}

	@RequestMapping("detail")
	@ResponseBody
	public JsonResult detail(String id) {
		Repository repository = sqlHelper.findById(id, Repository.class);
		return renderSuccess(repository);
	}

	@Transactional
	@RequestMapping("del")
	@ResponseBody
	public JsonResult del(String id) {
		repositoryService.deleteById(id);
		configService.refresh();
		return renderSuccess();
	}

	@RequestMapping("userPermission")
	public ModelAndView userPermission(HttpServletRequest request, ModelAndView modelAndView, Page page, String repositoryId) {
		String port = settingService.get("port");

		page = repositoryService.userPermission(page, repositoryId);
		Repository repository = sqlHelper.findById(repositoryId, Repository.class);

		Page<RepositoryUserExt> pageExt = BeanExtUtil.copyPageByProperties(page, RepositoryUserExt.class);
		for (RepositoryUserExt repositoryUserExt : (List<RepositoryUserExt>) pageExt.getRecords()) {
			repositoryUserExt.setUser(sqlHelper.findById(repositoryUserExt.getUserId(), User.class));

			String url = "svn://" + getIP(request.getRequestURL().toString() + "/");
			if (!port.equals("3690")) {
				url += (":" + port);
			}
			url += ("/" + repository.getName() + repositoryUserExt.getPath());
			repositoryUserExt.setPath(url);
		}

		modelAndView.addObject("userList", sqlHelper.findAll(User.class));

		modelAndView.addObject("repositoryId", repositoryId);
		modelAndView.addObject("page", pageExt);
		modelAndView.setViewName("/adminPage/repository/userPermission");
		return modelAndView;
	}

	@Transactional
	@RequestMapping("addUser")
	@ResponseBody
	public JsonResult addUser(RepositoryUser repositoryUser) {
		if (repositoryService.hasUser(repositoryUser.getUserId(), repositoryUser.getPath(), repositoryUser.getRepositoryId(), repositoryUser.getId())) {
			return renderError("该用户路径授权已存在");
		}
		repositoryService.addUser(repositoryUser);
		configService.refresh();
		return renderSuccess();
	}

	@Transactional
	@RequestMapping("delUser")
	@ResponseBody
	public JsonResult delUser(String id) {
		repositoryService.delUser(id);
		configService.refresh();
		return renderSuccess();
	}

	@Transactional
	@RequestMapping("userDetail")
	@ResponseBody
	public JsonResult userDetail(String id) {

		return renderSuccess(sqlHelper.findById(id, RepositoryUser.class));
	}

	@RequestMapping("groupPermission")
	public ModelAndView groupPermission(HttpServletRequest request, ModelAndView modelAndView, Page page, String repositoryId) {
		String port = settingService.get("port");

		page = repositoryService.groupPermission(page, repositoryId);
		Repository repository = sqlHelper.findById(repositoryId, Repository.class);

		Page<RepositoryGroupExt> pageExt = BeanExtUtil.copyPageByProperties(page, RepositoryGroupExt.class);
		for (RepositoryGroupExt repositoryGroupExt : (List<RepositoryGroupExt>) pageExt.getRecords()) {
			repositoryGroupExt.setGroup(sqlHelper.findById(repositoryGroupExt.getGroupId(), Group.class));

			String url = "svn://" + getIP(request.getRequestURL().toString() + "/");
			if (!port.equals("3690")) {
				url += (":" + port);
			}
			url += ("/" + repository.getName()  + repositoryGroupExt.getPath());
			repositoryGroupExt.setPath(url);
		}

		modelAndView.addObject("groupList", sqlHelper.findAll(Group.class));

		modelAndView.addObject("repositoryId", repositoryId);
		modelAndView.addObject("page", pageExt);
		modelAndView.setViewName("/adminPage/repository/groupPermission");
		return modelAndView;
	}

	@Transactional
	@RequestMapping("addGroup")
	@ResponseBody
	public JsonResult addGroup(RepositoryGroup repositoryGroup) {
		if (repositoryService.hasGroup(repositoryGroup.getGroupId(), repositoryGroup.getPath(), repositoryGroup.getRepositoryId(), repositoryGroup.getId())) {
			return renderError("该小组路径授权已存在");
		}

		repositoryService.addGroup(repositoryGroup);
		configService.refresh();
		return renderSuccess();
	}

	@Transactional
	@RequestMapping("delGroup")
	@ResponseBody
	public JsonResult delGroup(String id) {
		repositoryService.delGroup(id);
		configService.refresh();
		return renderSuccess();
	}

	@Transactional
	@RequestMapping("groupDetail")
	@ResponseBody
	public JsonResult groupDetail(String id) {

		return renderSuccess(sqlHelper.findById(id, RepositoryGroup.class));
	}

	@Transactional
	@RequestMapping("loadOver")
	@ResponseBody
	public JsonResult loadOver(String id, String dirTemp) {

		Repository repository = sqlHelper.findById(id, Repository.class);

		String rs = "";
		String home = sqlConfig.home;

		if (SystemTool.isWindows()) {

//			if (!home.contains(":")) {
//				// 获取盘符
//				home = JarUtil.getCurrentFilePath().split(":")[0] + ":" + home;
//			}

			String cmd = "svnadmin.exe load " + (home + File.separator + "repo" + File.separator + repository.getName() + File.separator).replace("/", "\\") + " < " + dirTemp;
			FileUtil.writeString(cmd, home + File.separator + "dump.bat", CharsetUtil.GBK);

			rs = RuntimeUtil.execForStr(home + File.separator + "dump.bat");
		} else {
			String sh = "svnadmin load " + (home + File.separator + "repo" + File.separator + repository.getName() + File.separator) + " < " + dirTemp;
			FileUtil.writeString(sh, home + File.separator + "dump.sh", CharsetUtil.CHARSET_UTF_8);

			rs = RuntimeUtil.execForStr("sh", home + File.separator + "dump.sh");
		}

		FileUtil.del(dirTemp);
		System.out.println(rs);

		return renderSuccess(rs.replace("\n", "<br>"));
	}

	@Transactional
	@RequestMapping("dumpOver")
	@ResponseBody
	public void dumpOver(String id, HttpServletRequest request, HttpServletResponse response) throws Exception {
		Repository repository = sqlHelper.findById(id, Repository.class);

		String rs = "";
		String home = sqlConfig.home;
		String dumpTemp = null;

		if (SystemTool.isWindows()) {

//			if (!home.contains(":")) {
//				// 获取盘符
//				home = JarUtil.getCurrentFilePath().split(":")[0] + ":" + home;
//			}
			dumpTemp = home + File.separator + "temp" + File.separator + repository.getName() + ".dump";
			String cmd = "svnadmin.exe dump " + (home + File.separator + "repo" + File.separator + repository.getName() + File.separator).replace("/", "\\") + " > " + dumpTemp;

			FileUtil.writeString(cmd, home + File.separator + "dump.bat", CharsetUtil.GBK);

			rs = RuntimeUtil.execForStr(home + File.separator + "dump.bat");
		} else {

			dumpTemp = home + File.separator + "temp" + File.separator + repository.getName() + ".dump";
			String sh = "svnadmin dump " + (home + File.separator + "repo" + File.separator + repository.getName() + File.separator) + " > " + dumpTemp;

			FileUtil.writeString(sh, home + File.separator + "dump.sh", CharsetUtil.CHARSET_UTF_8);

			rs = RuntimeUtil.execForStr("sh", home + File.separator + "dump.sh");
		}

		System.out.println(rs);

		if (FileUtil.exist(dumpTemp)) {

			// 读到流中
			InputStream inputStream = new FileInputStream(dumpTemp);// 文件的存放路径
			response.reset();
			response.setContentType("application/octet-stream");
			String filename = new File(dumpTemp).getName();
			response.addHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"));
			ServletOutputStream outputStream = response.getOutputStream();
			byte[] b = new byte[1024];
			int len;
			// 从输入流中读取一定数量的字节，并将其存储在缓冲区字节数组中，读到末尾返回-1
			while ((len = inputStream.read(b)) > 0) {
				outputStream.write(b, 0, len);
			}
			inputStream.close();

			FileUtil.del(dumpTemp);
		}

	}

	@Transactional
	@RequestMapping("getFileList")
	@ResponseBody
	public JsonResult getFileList(String id) {

		List<Path> paths = pathUtls.getPath(id);

		return renderSuccess(paths);
	}

	@Transactional
	@RequestMapping("see")
	@ResponseBody
	public ModelAndView see(ModelAndView modelAndView, String repositoryId, String id) {
		Repository repository = sqlHelper.findById(repositoryId, Repository.class);
		List<Path> paths = pathUtls.getPath(repositoryId);

		Path path = findPathById(id, paths);

		String filePath = path.getName();
		while (StrUtil.isNotEmpty(path.getpId())) {
			path = findPathById(path.getpId(), paths);
			filePath = path.getName() + "/" + filePath;
		}

		System.out.println(filePath);

		String home = sqlConfig.home;
		String rs = null;

		if (SystemTool.isWindows()) {
//			if (!home.contains(":")) {
//				// 获取盘符
//				home = JarUtil.getCurrentFilePath().split(":")[0] + ":" + home;
//			}
			String cmd = "svnlook.exe cat " + (home + File.separator + "repo" + File.separator + repository.getName() + File.separator).replace("/", "\\") + " " + filePath;
			rs = RuntimeUtil.execForStr(cmd);
		} else {
			String sh = "svnlook cat " + (home + File.separator + "repo" + File.separator + repository.getName() + File.separator) + " " + filePath;
			rs = RuntimeUtil.execForStr(sh);
		}

		modelAndView.addObject("rs", rs);
		modelAndView.setViewName("/adminPage/repository/see");
		return modelAndView;
	}

	private Path findPathById(String id, List<Path> paths) {
		for (Path path : paths) {
			if (path.getId().equals(id)) {
				return path;
			}
		}
		return null;
	}

	@RequestMapping("getUserList")
	@ResponseBody
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

	@RequestMapping("getGroupList")
	@ResponseBody
	public JsonResult getGroupList() {

		List<Group> groups = sqlHelper.findAll(Group.class);

		List<Select> selects = new ArrayList<>();
		for (Group group : groups) {
			Select select = new Select();
			select.setName(group.getName());
			select.setValue(group.getId());
			selects.add(select);
		}

		return renderSuccess(selects);
	}

}
