package com.cym.ext;

public class SVNFileEntry {
//	/**
//	 * 文件名
//	 */
//	private String name;
//	/**
//	 * 文件大小
//	 */
//	private Long size;
//	/**
//	 * 文件类别，文件file，文件夹dir
//	 */
//	private String kind;
//	/**
//	 * 版本号
//	 */
//	private Long revision;
	/**
	 * 操作文件类型：A添加，D删除，U更新
	 */
	private String type;
	/**
	 * 文件路径 ：如 /测试项目/112/中文.ppt
	 */
	private String path;

//	public String getName() {
//		return name;
//	}
//
//	public void setName(String name) {
//		this.name = name;
//	}
//
//	public Long getSize() {
//		return size;
//	}
//
//	public void setSize(Long size) {
//		this.size = size;
//	}
//
//	public String getKind() {
//		return kind;
//	}
//
//	public void setKind(String kind) {
//		this.kind = kind;
//	}
//
//	public Long getRevision() {
//		return revision;
//	}
//
//	public void setRevision(Long revision) {
//		this.revision = revision;
//	}


	public String getPath() {
		return path;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setPath(String path) {
		this.path = path;
	}

}