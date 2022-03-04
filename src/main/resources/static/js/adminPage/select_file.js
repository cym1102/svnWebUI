
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

