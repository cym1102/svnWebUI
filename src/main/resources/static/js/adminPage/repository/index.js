var load;
$(function() {
	layui.use('upload', function() {
		var upload = layui.upload;
		upload.render({
			elem: '#upload',
			url: '/adminPage/main/upload/',
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

	showLoad();
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/repository/addOver',
		data: {
			id: $("#id").val(),
			name: $("#name").val()
		},
		dataType: 'json',
		success: function(data) {
			closeLoad();
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
	if (confirm("确认删除?")) {
		showLoad();
		$.ajax({
			type: 'POST',
			url: ctx + '/adminPage/repository/del',
			data: {
				id: id
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
}




function groupPermission(id,name) {
	layer.open({
		type: 2,
		title: name + '-小组授权',
		area: ['1000px', '720px;'],
		content: ctx + "/adminPage/repository/groupPermission?repositoryId=" + id
	});
}

function userPermission(id,name) {
	layer.open({
		type: 2,
		title: name + '-用户授权',
		area: ['1000px', '720px;'],
		content: ctx + "/adminPage/repository/userPermission?repositoryId=" + id
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

var zTreeObj;
function seeFile(id) {
	$("#repositoryId").val(id);
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/repository/getFileList',
		data: {
			id: id
		},
		dataType: 'json',
		success: function(data) {
			if (data.success) {
				var zNodes = data.obj
				var setting = {
					data: {
						simpleData: {
							enable: true
						}
					}
				};
				console.log(zNodes)
				zTreeObj = $.fn.zTree.init($("#rootSelect"), setting, zNodes);
			
				layer.open({
					type: 1,
					title: "文件目录",
					area: ['600px', '620px'], // 宽高
					content: $('#rootSelectDiv')
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

function see() {
	var nodes = zTreeObj.getSelectedNodes();
	if (nodes.length > 0) {
		window.open(ctx + '/adminPage/repository/see?id=' + nodes[0].id + "&repositoryId=" + $("#repositoryId").val());
	} else {
		layer.msg("未选中文件");
	}

}