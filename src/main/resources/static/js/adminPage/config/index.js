$(function() {
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/config/getStatus',
		dataType: 'json',
		success: function(data) {
			if (data.success) {
				$("#status").html(data.obj);
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
				port: $("#port").val().trim()
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
