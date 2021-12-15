var load;
$(function(){
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
	$("#trueName").val("");
	$("#pass").val("");
	$("#type option:first").prop("select", true);
	form.render();
	showWindow("添加用户");
}


function showWindow(title){
	layer.open({
		type : 1,
		title : title,
		area : [ '500px', '400px' ], // 宽高
		content : $('#windowDiv')
	});
}

function addOver() {
	if ($("#name").val() == "") {
		layer.msg("登录名为空");
		return;
	}
	if ($("#trueName").val() == "") {
		layer.msg("姓名为空");
		return;
	}
	if ($("#pass").val() == "") {
		layer.msg("密码为空");
		return;
	}
	showLoad();
	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/user/addOver',
		data : $('#addForm').serialize(),
		dataType : 'json',
		success : function(data) {
			closeLoad();
			if (data.success) {
				location.reload();
			} else {
				layer.msg(data.msg);
			}
		},
		error : function() {
			closeLoad();
			alert("出错了,请联系技术人员!");
		}
	});
	
	
}

function edit(id) {
	showLoad();
	$.ajax({
		type : 'GET',
		url : ctx + '/adminPage/user/detail',
		dataType : 'json',
		data : {
			id : id
		},
		success : function(data) {
			closeLoad();
			if (data.success) {
				var user = data.obj;
				$("#id").val(user.id); 
				$("#name").val(user.name);
				$("#trueName").val(user.trueName);
				$("#pass").val(user.pass);
				$("#type").val(user.type);
				form.render();
				showWindow("编辑用户");
			}else{
				layer.msg(data.msg);
			}
		},
		error : function() {
			closeLoad();
			alert("出错了,请联系技术人员!");
		}
	});
}

function del(id){
	if(confirm("确认删除?")){
		showLoad();
		$.ajax({
			type : 'POST',
			url : ctx + '/adminPage/user/del',
			data : {
				id : id
			},
			dataType : 'json',
			success : function(data) {
				closeLoad();
				if (data.success) {
					location.reload();
				}else{
					layer.msg(data.msg)
				}
			},
			error : function() {
				closeLoad();
				alert("出错了,请联系技术人员!");
			}
		});
	}
}


var index;
function importUser() {
	index = layer.open({
		type: 1,
		title: "导入用户",
		area: ['600px', '300px'], // 宽高
		content: $('#importDiv')
	});
}

function importOver() {
	if ($("#dirTemp").val() == '') {
		layer.alert("未选择passwd文件");
		return;
	}

	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/user/importOver',
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
