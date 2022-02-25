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
		}
	},
	load: function() {
		rootSelect.zTreeObj = $.fn.zTree.init($("#rootSelect"), rootSelect.setting);
	},
	close: function() {
		layer.close(rootSelect.index);
	}
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
				var url = encodeURIComponent(data.obj.url);
				rootSelect.setting.async.url = ctx + '/adminPage/repository/getFileList?url=' + url;
				rootSelect.load();
				rootSelect.index = layer.open({
					type: 1,
					title: "选择路径",
					area: ['600px', '560px'], // 宽高
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

function selectOver() {
	var nodes = rootSelect.zTreeObj.getSelectedNodes();
	if (nodes.length > 0) {
		var url = decodeURIComponent(nodes[0].id);

		var relativePaths = [];
		var urls = url.split("/");
		for (let i = 4; i < urls.length; i++) {
			relativePaths.push(urls[i]);
		}

		$("#" + rootSelect.id).val("/" + relativePaths.join("/"));
	}
	layer.close(rootSelect.index);
}

function selectRootOver(){
	$("#" + rootSelect.id).val("/");
	layer.close(rootSelect.index);
}


