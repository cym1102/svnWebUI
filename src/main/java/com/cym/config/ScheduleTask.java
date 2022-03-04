package com.cym.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.schedule.annotation.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import com.cym.model.Repository;
import com.cym.model.WebHook;
import com.cym.service.SettingService;
import com.cym.service.WebHookService;
import com.cym.sqlhelper.utils.SqlHelper;
import com.cym.utils.PathUtls;
import com.cym.utils.SvnAdminUtils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;

@Component
public class ScheduleTask {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	@Inject
	SettingService settingService;
	@Inject
	SqlHelper sqlHelper;
	@Inject
	PathUtls pathUtls;
	@Inject
	SvnAdminUtils svnAdminUtils;
	@Inject
	WebHookService webHookService;

	Map<String, Long> versionMap = new HashMap<String, Long>();

	// 获取更新信息
	@Scheduled(cron = "0 * * * * ?")
	public void hookTasks() {
		WebHook webHook = webHookService.get();
		if (webHook.getOpen()) {
			List<Repository> list = sqlHelper.findAll(Repository.class);
			if (versionMap.size() == 0) {
				for (Repository repository : list) {
					versionMap.put(repository.getId(), getNewestVersion(repository));
				}
			} else {
				for (Repository repository : list) {
					Long newVersion = getNewestVersion(repository);
					Long oldVersion = versionMap.get(repository.getId());
					if (newVersion != null && oldVersion != null && newVersion > oldVersion) {
						sendHook(repository, oldVersion, newVersion);
						versionMap.put(repository.getId(), newVersion);
					}
				}
			}
		}
	}

	private void sendHook(Repository repository, Long oldVersion, Long newVersion) {
		WebHook webHook = webHookService.get();
		Map<String, Object> paramMap = new HashMap<String, Object>();

		String port = settingService.get("port");
		String url = pathUtls.buildUrl(port) + "/" + repository.getName();
		try {
			SVNRepository svnRepository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(svnAdminUtils.adminUserName, svnAdminUtils.adminUserPass.toCharArray());
			svnRepository.setAuthenticationManager(authManager);
			svnRepository.log(new String[] { "" }, oldVersion, newVersion, true, true, new ISVNLogEntryHandler() {
				@Override
				public void handleLogEntry(SVNLogEntry svnlogentry) throws SVNException {
					if (svnlogentry.getRevision() > oldVersion) {
						paramMap.put("repository", repository.getName());
						paramMap.put("author", svnlogentry.getAuthor());
						paramMap.put("commitMessage", svnlogentry.getMessage());
						paramMap.put("revision", svnlogentry.getRevision());
						paramMap.put("password", webHook.getPassword());
						paramMap.put("time", DateUtil.format(svnlogentry.getDate(), "yyyy-MM-dd HH:mm:ss"));
						
						String json = JSONUtil.toJsonPrettyStr(paramMap);
						HttpUtil.post(webHook.getUrl(), json, 2000);
					}
				}
			});

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private Long getNewestVersion(Repository repository) {
		String port = settingService.get("port");
		String url = pathUtls.buildUrl(port) + "/" + repository.getName();
		try {
			SVNRepository svnRepository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(svnAdminUtils.adminUserName, svnAdminUtils.adminUserPass.toCharArray());
			svnRepository.setAuthenticationManager(authManager);
			return svnRepository.getLatestRevision();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

}