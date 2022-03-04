$(function() {

});


function changePass() {
	$("#oldPass").val("");
	$("#newPass").val("");
	$("#repeatPass").val("");

	layer.open({
		type: 1,
		title: "修改密码",
		area: ['500px', '400px'], // 宽高
		content: $('#windowDiv')
	});

}


function changeOver() {
	var oldPass = $("#oldPass").val();
	var newPass = $("#newPass").val();
	var repeatPass = $("#repeatPass").val();

	if (oldPass == '' || newPass == '' || repeatPass == '') {
		layer.msg("填写不完整");
		return;
	}

	if (newPass != repeatPass) {
		layer.msg("两次输入不相同");
		return;
	}
	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/info/changeOver',
		data : $('#addForm').serialize(),
		dataType : 'json',
		success : function(data) {
			closeLoad();
			if (data.success) {
				layer.closeAll();
				layer.msg("修改成功");
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

function download() {
	var nodes = fileSelect.zTreeObj.getSelectedNodes();
	if (nodes.length > 0) {
		window.open(ctx + '/adminPage/info/download?url=' + encodeURIComponent(nodes[0].id));
	} else {
		layer.msg("未选中文件");
	}
}


function seeFile(url) {
	url = encodeURIComponent(url);
	fileSelect.setting.async.url = ctx + '/adminPage/info/getFileList?url=' + url;
	fileSelect.load();
	layer.open({
		type: 1,
		title: "文件目录",
		area: ['600px', '560px'], // 宽高
		content: $('#fileSelectDiv')
	});
}
