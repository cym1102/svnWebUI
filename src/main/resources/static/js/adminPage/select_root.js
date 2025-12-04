var orgUrl;
var rootSelect = {
	id: null,
	index: null,
	zTreeObj: null,
	setting: {
		async: {
			enable: true,
			dataType: "json",
			url: '',
			autoParam: ["id"]
		},
		data: {
			simpleData: {
				enable: true,
				idKey: 'id',
				pIdKey: 'pid',
				rootPId: ''
			}
		},
		check: {
			enable: true,
			chkStyle: "checkbox",
			chkboxType: { "Y": "", "N": "" }
		},
		callback: {
			onClick: function(event, treeId, treeNode) {
				var svnUrl = decodeURIComponent(treeNode.id);
				$("#svnUrl").html(svnUrl);
			}
		}
	},
	load: function() {
		rootSelect.zTreeObj = $.fn.zTree.init($("#rootSelect"), rootSelect.setting);
	},
	close: function() {
		layer.close(rootSelect.index);
	}
}

var load;
$(function() {
	layui.use('upload', function() {
		var upload = layui.upload;
		upload.render({
			elem: '#upload',
			url: '/adminPage/main/upload',
			accept: 'file',
			before: function(res) {
				load = layer.load();
			},
			done: function(res) {
				layer.close(load);
				// 上传完毕回调
				if (res.success) {
					sendFile(res.obj);
				}
			},
			error: function() {
				layer.close(load);
				// 请求异常回调
			}
		});
	});
})

function sendFile(filePath) {
	// 获取选中的路径
	var target = $("#svnUrl").html();
	var nodes = rootSelect.zTreeObj.getSelectedNodes();
	if (nodes.length > 0) {
		target = decodeURIComponent(nodes[0].id);
	}

	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/selectRoot/upload',
		data: {
			filePath: filePath,
			url: target
		},
		dataType: 'json',
		success: function(data) {
			if (data.success) {
				rootSelect.load();
			} else {
				layer.msg(data.msg);
			}
		},
		error: function() {
			layer.alert("出错了,请联系技术人员!");
		}
	});
}


function cancelSelect() {
	$("#svnUrl").html(orgUrl);
	
	rootSelect.zTreeObj.cancelSelectedNode();
}

function selectRoot(id, repositoryId) {
	rootSelect.id = id;

	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/repository/detail',
		data: {
			id: repositoryId
		},
		dataType: 'json',
		success: function(data) {
			if (data.success) {
				$("#selectOver").show();
				$("#selectRootOver").show();
				$("#uploadDiv").show();
				$("#mkdir").show();
				$("#rmfile").show();
				$("#download").hide();
				$("#rmfile").hide();

				showTree(data.obj.url, true);
			} else {
				layer.msg(data.msg);
			}
		},
		error: function() {
			layer.alert("出错了,请联系技术人员!");
		}
	});
}

function seeFile(url, permission) {
	$("#selectOver").hide();
	$("#selectRootOver").hide();
	$("#download").show();
	$("#rmfile").show();

	if (permission == 'rw') {
		$("#uploadDiv").show();
		$("#mkdir").show();
		$("#rmfile").show();
	} else {
		$("#uploadDiv").hide();
		$("#mkdir").hide();
		$("#rmfile").hide();
	}
	showTree(url, false);
}


function showTree(url, check) {
	$("#svnUrl").html(url);
	orgUrl = url;
	rootSelect.setting.async.url = ctx + '/adminPage/selectRoot/getFileList?url=' + encodeURIComponent(url) + "&guid=" + guid();
	rootSelect.setting.check.enable = check;
	rootSelect.load();
	rootSelect.index = layer.open({
		type: 1,
		title: "svn目录",
		area: ['800px', '600px'], // 宽高
		content: $('#rootSelectDiv')
	});
}


function mkdir() {
	layer.prompt({
		value: '',
		title: '文件夹名称',
		area: ['300px', '100px'] //自定义文本域宽高
	}, function(value, index, elem) {

		var target = $("#svnUrl").html() + "/" + value;
		var nodes = rootSelect.zTreeObj.getSelectedNodes();
		if (nodes.length > 0) {
			target = decodeURIComponent(nodes[0].id) + "/" + value;
		}

		$.ajax({
			type: 'POST',
			url: ctx + '/adminPage/selectRoot/mkdir',
			data: {
				url: target
			},
			dataType: 'json',
			success: function(data) {
				if (data.success) {
					layer.close(index); // 关闭prompt框
					rootSelect.load();
				} else {
					layer.msg(data.msg);
				}
			},
			error: function() {
				layer.alert("出错了,请联系技术人员!");
			}
		});

	});
}



function rmfile() {
	if (confirm("确认删除文件?")) {
		debugger
		// 获取选中的路径
		var value = '';
		var nodes = rootSelect.zTreeObj.getSelectedNodes();
		if (nodes.length > 0) {
			value = decodeURIComponent(nodes[0].id);
		}

		if (value == '') {
			layer.msg("未选中任何文件");
		}

		$.ajax({
			type: 'POST',
			url: ctx + '/adminPage/selectRoot/rmfile',
			data: {
				url: value
			},
			dataType: 'json',
			success: function(data) {
				if (data.success) {
					rootSelect.load();
				} else {
					layer.msg(data.msg);
				}
			},
			error: function() {
				layer.alert("出错了,请联系技术人员!");
			}
		});

	}
}





function selectOver() {
	var nodes = rootSelect.zTreeObj.getCheckedNodes();
	if (nodes.length > 0) {
		var arrays = [];
		for (let i = 0; i < nodes.length; i++) {
			if (!nodes[i].getCheckStatus().half) {

				var url = decodeURIComponent(nodes[i].id);

				var relativePaths = [];
				var urls = url.split("/");
				for (let i = 4; i < urls.length; i++) {
					relativePaths.push(urls[i]);
				}
				arrays.push("/" + relativePaths.join("/"));
			}
		}

		$("#" + rootSelect.id).val(arrays.join(";"));
	}
	layer.close(rootSelect.index);
}

function selectRootOver() {
	$("#" + rootSelect.id).val("/");
	layer.close(rootSelect.index);
}


function download() {
	var nodes = rootSelect.zTreeObj.getSelectedNodes();
	if (nodes.length > 0) {
		if (nodes[0].isParent) {
			layer.msg("目录不可下载");
			return;
		}

		window.open(ctx + '/adminPage/selectRoot/download?url=' + encodeURIComponent(nodes[0].id));
	} else {
		layer.msg("未选中文件");
	}
}

function copyUrl() {
	var textArea = document.createElement("textarea");
	textArea.style.position = 'fixed';
	textArea.style.top = '0';
	textArea.style.left = '0';
	textArea.style.width = '2em';
	textArea.style.height = '2em';
	textArea.style.padding = '0';
	textArea.style.border = 'none';
	textArea.style.outline = 'none';
	textArea.style.boxShadow = 'none';
	textArea.style.background = 'transparent';
	textArea.value = $("#svnUrl").html();
	document.body.appendChild(textArea);
	textArea.select();

	try {
		var successful = document.execCommand('copy');
		var msg = successful ? '成功复制到剪贴板' : '该浏览器不支持点击复制到剪贴板';
		layer.msg(msg);
	} catch (err) {
		layer.msg('该浏览器不支持点击复制到剪贴板');
	}

	document.body.removeChild(textArea);
}
