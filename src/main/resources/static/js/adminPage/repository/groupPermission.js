var groupId;
$(function(){
	layui.config({
		base: ctx + '/lib/layui/exts/xmSelect/'
	}).extend({
		xmSelect: 'xm-select'
	}).use(['xmSelect'], function(){
		var xmSelect = layui.xmSelect;
		
		$.ajax({
			type : 'GET',
			url : ctx + '/adminPage/repository/getGroupList',
			success : function(data) {
				if (data) {
					groupId = xmSelect.render({
					    el: '#groupId', 
					    name : 'groupId',
						filterable: true,
					    model: { label: { type: 'text' } },
					    radio: true,
					    clickClose: true,
					    data: data.obj
					})
				}else{
					layer.msg(data.msg);
				}
			},
			error : function() {
				layer.alert("出错了,请联系技术人员!");
			}
		});
	})
})


function search() {
	$("input[name='curr']").val(1);
	$("#searchForm").submit();
}

function add() {
	$("#id").val(""); 
	$("#trueName").val("");
	$("#pass").val("");
	$("#path").val("/");
	groupId.setValue([""]);
	$("#permission option:first").prop("selected", true);
	form.render();
	showWindow("添加小组");
}


function showWindow(title){
	layer.open({
		type : 1,
		title : title,
		area : [ '550px', '400px' ], // 宽高
		content : $('#windowDiv')
	});
}

function addOver() {
	if($("#path").val() == ''){
		layer.msg("路径为空");
		return;
	}
	if(groupId.getValue() == "" || groupId.getValue() == null){
		layer.msg("小组为空");
		return;
	}
	
	showLoad();
	$.ajax({
		type : 'POST',
		url : ctx + '/adminPage/repository/addGroup',
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

/*
function edit(id) {
	showLoad();
	$.ajax({
		type : 'GET',
		url : ctx + '/adminPage/repository/groupDetail',
		dataType : 'json',
		data : {
			id : id
		},
		success : function(data) {
			closeLoad();
			if (data.success) {
				var repositoryGroup = data.obj;
				$("#id").val(repositoryGroup.id); 
				$("#path").val(repositoryGroup.path); 
				groupId.setValue([repositoryGroup.groupId]);
				$("#permission").val(repositoryGroup.permission);
				form.render();
				showWindow("编辑小组");
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
*/


function del(id){
	if(confirm("确认删除?")){
		showLoad();
		$.ajax({
			type : 'POST',
			url : ctx + '/adminPage/repository/delGroup',
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


