$(function() {
	form.on('switch(enable)', function(data) {

		$.ajax({
			type: 'POST',
			url: ctx + '/adminPage/repository/setEnable',
			data: {
				enable: data.elem.checked ? 1 : 0,
				id: data.elem.value
			},
			dataType: 'json',
			success: function(data) {

			},
			error: function() {
				layer.alert(commonStr.errorInfo);
			}
		});
	});

})


function search() {
	$("input[name='curr']").val(1);
	$("#searchForm").submit();
}

function add() {
	$("#id").val("");
	$("#name").val("");

	form.render();
	showWindow("添加仓库");
}

function scan() {
	if (confirm("请将仓库文件夹放到 " + $("#home").val() + "repo/ 下，再点击确定进行扫描。")) {
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
	if (hasSpec($("#name").val())) {
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
		area: ['1000px', '660px;'],
		content: ctx + "/adminPage/repository/groupPermission?repositoryId=" + id
	});
}

function userPermission(id, name) {
	layer.open({
		type: 2,
		title: name + '-用户授权',
		area: ['1000px', '660px;'],
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


function editMark(id) {
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/repository/detail',
		data: {
			id: id
		},
		dataType: 'json',
		success: function(data) {
			if (data.success) {
				$("#repositoryId").val(id);
				$("#mark").val(data.obj.mark != null ? data.obj.mark : '');

				layer.open({
					type: 1,
					title: "描述",
					area: ['500px', '360px'], // 宽高
					content: $('#markDiv')
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


function editMarkOver() {
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/repository/editMark',
		data: {
			id: $("#repositoryId").val(),
			mark: $("#mark").val()
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
			layer.alert("出错了,请联系技术人员!");
		}
	});
}

function seeLog(url) {
	layer.open({
		type: 2,
		title: '查看日志 ' + url,
		area: ['1000px', '630px;'],
		content: ctx + "/adminPage/seeLog?url=" + url
	});
}

function copyPermission(id, name) {
	$("#toId").val(id);
	$("#toName").html(name);
	
	layer.open({
		type: 1,
		title: '权限拷贝',
		area: ['500px', '400px;'],
		content: $('#copyPermissionDiv')
	});
}

function copyPermissionOver(){
	if($("#fromId").val() == null){
		layer.msg("来源仓库未选择");
		return;
	}
	showLoad();
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/repository/copyPermissionOver',
		data: {
			toId: $("#toId").val(),
			fromId: $("#fromId").val()
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
			layer.alert("出错了,请联系技术人员!");
		}
	});
	
}

function copyRepo(id, name){
	$("#copyName").val("");
	
	layer.open({
		type: 1,
		title: '复制仓库',
		area: ['500px', '400px;'],
		content: $('#copyRepoDiv')
	});
}

function copyRepoOver(){
	if($("#copyName").val() == ''){
		layer.msg("仓库名未填写");
		return;
	}
	if($("#fromCopyId").val() == null){
		layer.msg("来源仓库未选择");
		return;
	}
	showLoad();
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/repository/copyRepoOver',
		data: {
			copyName: $("#copyName").val(),
			fromCopyId: $("#fromCopyId").val()
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
			layer.alert("出错了,请联系技术人员!");
		}
	});
	
}