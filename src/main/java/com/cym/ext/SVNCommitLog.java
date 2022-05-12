package com.cym.ext;

import java.util.Date;
import java.util.List;

public class SVNCommitLog {
	/**
	 * 版本号
	 */
	private Long revision;
	/**
	 * 提交人信息
	 */
	private String author;
	/**
	 * 提交时间
	 */
	private String commitDate;
	/**
	 * 提交信息
	 */
	private String message;
//	/**
//	 * 仓库前缀
//	 */
//	private String repoPrefix;
	/**
	 * 文件列表
	 */
	private List<SVNFileEntry> fileList;

	public Long getRevision() {
		return revision;
	}

	public void setRevision(Long revision) {
		this.revision = revision;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}


	public String getCommitDate() {
		return commitDate;
	}

	public void setCommitDate(String commitDate) {
		this.commitDate = commitDate;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

//	public String getRepoPrefix() {
//		return repoPrefix;
//	}
//
//	public void setRepoPrefix(String repoPrefix) {
//		this.repoPrefix = repoPrefix;
//	}

	public List<SVNFileEntry> getFileList() {
		return fileList;
	}

	public void setFileList(List<SVNFileEntry> fileList) {
		this.fileList = fileList;
	}

}
