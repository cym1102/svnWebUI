$(function() {
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/config/getStatus',
		dataType: 'json',
		success: function(data) {
			if (data.success) {
				$("#status").html(data.obj);
				
				if(data.obj.indexOf('已启动') > -1){
					$("#start").hide();
					$("#stop").show();
				} else {
					$("#start").show();
					$("#stop").hide();
				}
			} else {
				alert(data.msg);
			}
		},
		error: function() {
			alert("出错了,请联系技术人员!");
		}
	});


	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/config/getWebHook',
		dataType: 'json',
		success: function(data) {
			if (data.success) {
				var webHook = data.obj;
				$("#open").val(webHook.open.toString());
				$("#url").val(webHook.url);
				$("#password").val(webHook.password);

				form.render();
			} else {
				alert(data.msg);
			}
		},
		error: function() {
			alert("出错了,请联系技术人员!");
		}
	});

});


function start() {
	if (confirm("确定启动？")) {

		if ($("#port").val().trim() == '') {
			layer.msg("启动端口未设置");
			return;
		}

		$.ajax({
			type: 'POST',
			url: ctx + '/adminPage/config/start',
			data: {
				port: $("#port").val().trim(),
				host: $("#host").val().trim(),
				protocol : $("#protocol").val().trim()
			},
			dataType: 'json',
			success: function(data) {
				if (data.success) {
					location.reload();
				} else {
					alert(data.msg);
				}
			},
			error: function() {
				alert("出错了,请联系技术人员!");
			}
		});
	}
}

function stop() {
	if (confirm("确定停止？")) {
		$.ajax({
			type: 'POST',
			url: ctx + '/adminPage/config/stop',
			dataType: 'json',
			success: function(data) {
				if (data.success) {
					location.reload();
				} else {
					alert(data.msg);
				}
			},
			error: function() {
				alert("出错了,请联系技术人员!");
			}
		});
	}
}


function save() {
	if (confirm("确定保存？")) {
		if ($("#url").val().trim() == '') {
			layer.msg("url链接未填写");
			return;
		}
		if ($("#password").val().trim() == '') {
			layer.msg("验证密码未填写");
			return;
		}

		$.ajax({
			type: 'POST',
			url: ctx + '/adminPage/config/saveHook',
			data: {
				open: $("#open").val(),
				password: $("#password").val(),
				url: $("#url").val()
			},
			dataType: 'json',
			success: function(data) {
				if (data.success) {
					location.reload();
				} else {
					alert(data.msg);
				}
			},
			error: function() {
				alert("出错了,请联系技术人员!");
			}
		});
	}
}

function readme() {
	layer.alert(`
<pre>
Webhook数据发送格式:

POST: {
    "repository" : "仓库名",
    "author" : "作者",
    "commitMessage" : "提交信息",
    "revision" : "最新版本",
    "time": "提交时间",
    "password" : "验证密码"
  }
</pre>
	`);

}