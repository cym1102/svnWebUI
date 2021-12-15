package com.cym.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cym.config.SqlConfig;
import com.cym.ext.Path;
import com.cym.model.Repository;
import com.cym.service.RepositoryService;

import cn.craccd.sqlHelper.utils.SqlHelper;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

@Component
public class PathUtls {
	@Autowired
	SqlConfig sqlConfig;
	@Autowired
	SqlHelper sqlHelper;
	@Autowired
	RepositoryService repositoryService;

	public List<Path> getPath(String repositoryId) {
		Repository repository = sqlHelper.findById(repositoryId, Repository.class);

		String home = sqlConfig.home;
		List<String> lines = null;
		if (SystemTool.isWindows()) {

			if (!home.contains(":")) {
				// 获取盘符
				home = JarUtil.getCurrentFilePath().split(":")[0] + ":" + home;
			}
			String cmd = "svnlook.exe tree " + (home + File.separator + "repo" + File.separator + repository.getName() + File.separator).replace("/", "\\");

			FileUtil.writeString(cmd, home + File.separator + "tree.bat", CharsetUtil.GBK);

			lines = RuntimeUtil.execForLines(home + File.separator + "tree.bat");
		} else {
			String cmd = "svnlook tree " + (home + File.separator + "repo" + File.separator + repository.getName() + File.separator);
			
			FileUtil.writeString(cmd, home + File.separator + "tree.sh", CharsetUtil.UTF_8);
			lines = RuntimeUtil.execForLines("sh", home + File.separator + "tree.sh");
		}
		// 删除多余输出
		List<String> removes = new ArrayList<>();
		for (String line : lines) {
			if (!line.equals("/")) {
				removes.add(line);
			} else {
				break;
			}
		}
		for (String remove : removes) {
			lines.remove(remove);
		}

		// 构建path
		List<Path> paths = new ArrayList<>();
		paths.add(buildPath("/", "/"));

		String blank = " ";
		while (hasBlankFile(lines, blank)) {
			for (int i = 0; i < lines.size(); i++) {
				if (lines.get(i).replace(lines.get(i).trim(), "").equals(blank)) {
					Path parentPath = findParentPath(lines, i, lines.get(i), paths);
					Path path = buildPath(lines.get(i), parentPath.getFullPath() + lines.get(i).replace(" ", ""));
					parentPath.getChildren().add(path);

					// 按文件夹排序
					Collections.sort(parentPath.getChildren(), new Comparator<Path>() {
						@Override
						public int compare(Path o1, Path o2) {
							// 升序排序，降序反写
							if (o1.getIsParent().equals("true") && o2.getIsParent().equals("false")) {
								return -1;
							}
							if (o1.getIsParent().equals("false") && o2.getIsParent().equals("true")) {
								return 1;
							}
							return o1.getName().compareToIgnoreCase(o2.getName());
						}
					});
				}
			}
			blank += " ";
		}

		return paths;
	}

	private boolean hasBlankFile(List<String> lines, String blank) {

		Boolean hasBlankFile = false;
		for (String line : lines) {
			if (line.replace(line.trim(), "").equals(blank)) {
				hasBlankFile = true;
				break;
			}
		}

		return hasBlankFile;
	}

	private Path findParentPath(List<String> lines, int start, String line, List<Path> paths) {
		for (int i = start; i >= 0; i--) {
			if ((lines.get(i).replace(lines.get(i).trim(), "") + " ").equals(line.replace(line.trim(), ""))) {
				String id = lines.get(i).replace(" ", "_");

				return findById(id, paths);

			}
		}
		return null;
	}

	private Path findById(String id, List<Path> paths) {
		Path pathRs = null;
		for (Path path : paths) {
			if (path.getId().equals(id)) {
				pathRs = path;
				break;
			}
			if (path.getChildren().size() > 0) {
				pathRs = findById(id, path.getChildren());
				if (pathRs != null) {
					break;
				}
			}
		}

		return pathRs;
	}

	private Path buildPath(String line, String full) {
		Path path = new Path();
		path.setIsParent(line.endsWith("/") ? "true" : "false");
		path.setId(line.replace(" ", "_"));
		path.setName(line.trim());
		path.setOpen(line.equals("/") ? "true" : "false");
		path.setFullPath(full);
		path.setChildren(new ArrayList<>());
		return path;
	}

}
