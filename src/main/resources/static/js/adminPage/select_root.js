var rootSelect = {
	id: null,
	index: null,
	zTreeObj : null,
	selectRoot: function(id, repositoryId) {
		this.id = id;
		$.ajax({
			type: 'POST',
			url: ctx + '/adminPage/repository/getFileList',
			data: {
				id: repositoryId
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
					rootSelect.zTreeObj = $.fn.zTree.init($("#rootSelect"), setting, zNodes);

					rootSelect.index = layer.open({
						type: 1,
						title: "选择路径",
						area: ['600px', '610px'], // 宽高
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
	},
	selectOver: function() {
		var nodes = rootSelect.zTreeObj.getSelectedNodes();
		if (nodes.length > 0) {
			$("#" + rootSelect.id).val(nodes[0].fullPath);
		}

		layer.close(rootSelect.index);
	},
	close: function() {
		layer.close(rootSelect.index);
	}
}

