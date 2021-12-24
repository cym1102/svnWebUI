var load;
var userIds;

$(function() {
	layui.config({
		base: ctx + 'lib/layui/exts/xmSelect/'
	}).extend({
		xmSelect: 'xm-select'
	}).use(['xmSelect'], function(){
		var xmSelect = layui.xmSelect;
		
		$.ajax({
			type : 'GET',
			url : ctx + '/adminPage/group/getUserList',
			success : function(data) {
				if (data) {
					userIds = xmSelect.render({
					    el: '#userIds', 
					    name : 'userIds',
						filterable: true,
					    model: { label: { type: 'text' } },
					    radio: false,
					    clickClose: false,
					    data: data.obj
					})
				}else{
					layer.msg(data.msg);
				}
			},
			error : function() {
				layer.alert('出错了,请联系技术人员!');
			}
		});
	})
	
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

	userIds.setValue([""]);

	form.render();
	showWindow("添加小组");
}


function showWindow(title) {
	layer.open({
		type: 1,
		title: title,
		area: ['600px', '400px'], // 宽高
		content: $('#windowDiv')
	});
}

function addOver() {
	if ($("#name").val() == "") {
		layer.msg("小组名为空");
		return;
	}

	showLoad();
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/group/addOver',
		data: $('#addForm').serialize(),
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

function edit(id) {
	showLoad();
	$.ajax({
		type: 'GET',
		url: ctx + '/adminPage/group/detail',
		dataType: 'json',
		data: {
			id: id
		},
		success: function(data) {
			closeLoad();
			if (data.success) {
				var groupExt = data.obj;
				$("#id").val(groupExt.id);
				$("#name").val(groupExt.name);

				userIds.setValue(data.obj.userIds);

				form.render();
				showWindow("编辑小组");
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
			url: ctx + '/adminPage/group/del',
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


var index;
function importGroup() {
	index = layer.open({
		type: 1,
		title: "导入小组",
		area: ['600px', '300px'], // 宽高
		content: $('#importDiv')
	});
}

function importOver() {
	if ($("#dirTemp").val() == '') {
		layer.alert("未选择authz文件");
		return;
	}

	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/group/importOver',
		data: $('#importForm').serialize(),
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

