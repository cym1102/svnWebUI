package com.cym.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.ModelAndView;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;

import com.cym.ext.SVNCommitLog;
import com.cym.ext.SVNFileEntry;
import com.cym.model.User;
import com.cym.sqlhelper.bean.Page;
import com.cym.utils.BaseController;
import com.cym.utils.BeanExtUtil;
import com.cym.utils.JsonResult;
import com.cym.utils.PathUtls;
import com.cym.utils.SvnAdminUtils;

import cn.hutool.core.date.DateUtil;

@Controller
@Mapping("/adminPage/seeLog")
public class SeeLogController extends BaseController {
	@Inject
	SvnAdminUtils svnAdminUtils;
	@Inject
	PathUtls pathUtls;

	@Mapping("")
	public ModelAndView seeLog(String url, Page page) throws SVNException {
		User user = getLoginUser();

		String userName = user.getName();
		String userPass = user.getPass();
		if (user.getType() == 1) {
			userName = svnAdminUtils.adminUserName;
			userPass = svnAdminUtils.adminUserPass;
		}

		Page<SVNLogEntry> logPage = pathUtls.seeLog(url, userName, userPass, page);

		Page<SVNCommitLog> svnCommitLogPage = BeanExtUtil.copyPageByProperties(page, SVNCommitLog.class);
		svnCommitLogPage.getRecords().clear();
		for (SVNLogEntry svnLogEntry : (List<SVNLogEntry>) logPage.getRecords()) {
			SVNCommitLog svnCommitLog = new SVNCommitLog();
			svnCommitLog.setRevision(svnLogEntry.getRevision());
			svnCommitLog.setAuthor(svnLogEntry.getAuthor() != null ? svnLogEntry.getAuthor() : "");
			svnCommitLog.setMessage(svnLogEntry.getMessage() != null ? svnLogEntry.getMessage() : "");
			svnCommitLog.setCommitDate(DateUtil.format(svnLogEntry.getDate(), "yyyy-MM-dd HH:mm:ss"));

			svnCommitLog.setFileList(new ArrayList<SVNFileEntry>());

			if (svnLogEntry.getChangedPaths() != null && !svnLogEntry.getChangedPaths().isEmpty()) {
				for (Iterator<SVNLogEntryPath> paths = svnLogEntry.getChangedPaths().values().iterator(); paths.hasNext();) {
					SVNLogEntryPath svnLogEntryPath = paths.next();

					SVNFileEntry svnFileEntry = new SVNFileEntry();
					svnFileEntry.setType(String.valueOf(svnLogEntryPath.getType()));
					svnFileEntry.setPath(svnLogEntryPath.getPath());

					svnCommitLog.getFileList().add(svnFileEntry);
				}
			}

			svnCommitLogPage.getRecords().add(svnCommitLog);
		}

		
		ModelAndView modelAndView = new ModelAndView("/adminPage/see_log.html");
		modelAndView.put("url", url);
		modelAndView.put("page", svnCommitLogPage);
		return modelAndView;
	}
}
