
var fileSelect = {
	zTreeObj : null,
	setting : {
		async : {
			enable : true,
			dataType : "json",
			url: '',
			autoParam: ["id"]
		},
        data:{
            simpleData:{
                enable: true,
                idKey:'id',
                pIdKey:'pid',
                rootPId: ''
            }
        }
	},
	load : function() {
		fileSelect.zTreeObj = $.fn.zTree.init($("#fileSelect"), fileSelect.setting);
	}
}


//var zTreeObj;
function seeFile(url) {
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

function download() {
	var nodes = fileSelect.zTreeObj.getSelectedNodes();
	if (nodes.length > 0) {
		window.open(ctx + '/adminPage/repository/download?url=' + encodeURIComponent(nodes[0].id));
	} else {
		layer.msg("未选中文件");
	}
}
