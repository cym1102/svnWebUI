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
					var path = res.obj.split('/');

					$("#fileName").html(path[path.length - 1]);
					$("#dirTemp").val(res.obj);
				}

			},
			error: function() {
				layer.close(load);
				// 请求异常回调
			}
		});

	});

})


function search() {
	$("input[name='pageNum']").val(1);
	$("#searchForm").submit();
}

function add() {
	$("#id").val("");
	$("#name").val("");

	form.render();
	showWindow("添加仓库");
}

function scan() {
	if (confirm("是否扫描 " + $("#home").val() + "repo/ 下已存在仓库?")) {
		$.ajax({
			type: 'POST',
			url: ctx + '/adminPage/repository/scan',
			dataType: 'json',
			success: function(data) {
				if (data.success) {
					location.reload();
				} else {
					layer.msg(data.msg);
				}
			},
			error: function() {
				closeLoad();
				alert("出错了,请联系技术人员!");
			}
		});
	}
}

function showWindow(title) {
	layer.open({
		type: 1,
		title: title,
		area: ['400px', '200px'], // 宽高
		content: $('#windowDiv')
	});
}

function addOver() {
	if ($("#name").val() == "") {
		layer.msg("仓库名为空");
		return;
	}
	if ($("#name").val().indexOf(" ") > -1) {
		layer.msg("仓库名不能包含空格");
		return;
	}
	if (flag.test($("#name").val())) {
		layer.msg("仓库名不能包含特殊字符");
		return;
	}

	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/repository/addOver',
		data: {
			name: $("#name").val()
		},
		dataType: 'json',
		success: function(data) {
			if (data.success) {
				location.reload();
			} else {
				layer.msg(data.msg);
			}
		},
		error: function() {
			closeLoad();
			alert("出错了,请联系技术人员!");
		}
	});
}

function del(id) {
	$("#delId").val(id);
	$("#pass").val("");

	layer.open({
		type: 1,
		title: "删除仓库",
		area: ['400px', '200px'], // 宽高
		content: $('#delDiv')
	});
}

function delOver() {
	showLoad();
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/repository/del',
		data: {
			id: $("#delId").val(),
			pass: $("#pass").val()
		},
		dataType: 'json',
		success: function(data) {
			closeLoad();
			if (data.success) {
				location.reload();
			} else {
				layer.msg(data.msg)
			}
		},
		error: function() {
			closeLoad();
			alert("出错了,请联系技术人员!");
		}
	});

}




function groupPermission(id, name) {
	layer.open({
		type: 2,
		title: name + '-小组授权',
		area: ['1000px', '630px;'],
		content: ctx + "/adminPage/repository/groupPermission?repositoryId=" + id
	});
}

function userPermission(id, name) {
	layer.open({
		type: 2,
		title: name + '-用户授权',
		area: ['1000px', '630px;'],
		content: ctx + "/adminPage/repository/userPermission?repositoryId=" + id
	});
}

function allPermission(id, name) {
	$("#perId").val(id);
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/repository/detail',
		data: {
			id: id
		},
		dataType: 'json',
		success: function(data) {
			if (data.success) {
				var repository = data.obj;
				$("#allPermission").val(repository.allPermission);
				form.render();
				layer.open({
					type: 1,
					title: name + '-全体授权',
					area: ['400px', '300px;'],
					content: $('#allPermissionDiv')
				});


			} else {
				layer.msg(data.msg)
			}
		},
		error: function() {
			alert("出错了,请联系技术人员!");
		}
	});

}

function allPermissionOver() {

	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/repository/allPermissionOver',
		data: {
			id: $("#perId").val(),
			allPermission: $("#allPermission").val()
		},
		dataType: 'json',
		success: function(data) {
			if (data.success) {
				location.reload();
			} else {
				layer.msg(data.msg)
			}
		},
		error: function() {
			alert("出错了,请联系技术人员!");
		}
	});

}


var index;
function loadBak(id, name) {
	$("#loadId").val(id);
	$("#repoName").html(name);

	index = layer.open({
		type: 1,
		title: "导入备份",
		area: ['600px', '400px'], // 宽高
		content: $('#loadDiv')
	});
}

function loadOver() {
	if ($("#dirTemp").val() == '') {
		layer.alert("未选择备份文件");
		return;
	}
	showLoad();
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/repository/loadOver',
		data: $('#loadForm').serialize(),
		dataType: 'json',
		success: function(data) {
			if (data.success) {
				closeLoad();
				layer.close(index);
				layer.open({
					type: 0,
					area: ['800px', '600px'],
					content: data.obj
				});
			} else {
				layer.msg(data.msg);
			}


		},
		error: function() {
			layer.alert("出错了,请联系技术人员!");
		}
	});
}

function dumpBak(id) {
	window.open(ctx + '/adminPage/repository/dumpOver?id=' + id);
}


function download() {
	var nodes = fileSelect.zTreeObj.getSelectedNodes();
	if (nodes.length > 0) {
		window.open(ctx + '/adminPage/repository/download?url=' + encodeURIComponent(nodes[0].id));
	} else {
		layer.msg("未选中文件");
	}
}

function mkdir() {
	layer.prompt({
		value: '',
		title: '文件夹名',
		area: ['300px', '100px'] //自定义文本域宽高
	}, function(value, index, elem) {
		//alert(value); //得到value
		
		$.ajax({
			type: 'POST',
			url: ctx + '/adminPage/repository/addFileDir',
			data: {
				svnUrl : svnUrl ,
				dir: value
			},
			dataType: 'json',
			success: function(data) {
				if (data.success) {
					layer.close(index);
					fileSelect.load();
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

var svnUrl;
function seeFile(url) {
	svnUrl = url;
	url = encodeURIComponent(url);
	fileSelect.setting.async.url = ctx + '/adminPage/repository/getFileList?url=' + url;
	fileSelect.load();
	layer.open({
		type: 1,
		title: "文件目录",
		area: ['600px', '560px'], // 宽高
		content: $('#fileSelectDiv')
	});
}
