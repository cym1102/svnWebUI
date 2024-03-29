var layer;
var element;
var form;
var laypage;
var laydate;

// 使用layui内部jQuery
var $ = layui.$;
var jQuery = layui.$;

$(function() {

	// layer变量
	layer = layui.layer;
	element = layui.element;
	form = layui.form;
	laypage = layui.laypage;

	// 执行一个laypage实例
	laypage.render({
		elem : 'pageInfo', // 渲染节点
		count : page.count, // 总记录数
		curr : page.curr, // 起始页
		limit : page.limit, // 每页记录数
		layout : [ 'prev', 'page', 'next','limit',  'skip' ],
		jump : function(obj, first) {
			// 首次不执行
			if (!first) {
				// do something
				$("input[name='curr']").val(obj.curr);
				$("input[name='limit']").val(obj.limit);
				$("#searchForm").submit();
			}
		}
	});
	
	// 日期控件
	layui.use('laydate', function() {
		laydate = layui.laydate;

		// 执行laydate实例
		$(".laydate").each(function(){
			$(this).attr("id", "date_" + guid());
			$(this).attr("readonly",true);
			
			laydate.render({
				elem : "#" + $(this).attr("id"), // 指定元素
				type : 'date',
				trigger: 'click',
				format : 'yyyy-MM-dd' // 可任意组合
			}); 
		})
		
		$(".laydatetime").each(function(){
			$(this).attr("id", "date_" + guid());
			$(this).attr("readonly",true);
			
			laydate.render({
				elem : "#" + $(this).attr("id"), // 指定元素
				type : 'datetime',
				trigger: 'click',
				format : 'yyyy-MM-dd HH:mm:ss' // 可任意组合
			}); 
		})
	});

	form.render();

	// 关闭input自动填充
	$("input").attr("autocomplete", "off");
})

// 关闭AJAX相应的缓存
$.ajaxSetup({
	cache : false
});


function gohref(url) {
	location.href = url;
	
}

// 退出登录
function loginOut() {
	if (confirm("是否退出登录?")) {
		location.href = ctx + "/adminPage/login/loginOut";
	}
}

// 日期格式化
Date.prototype.format = function(format) {
	var date = {
		"M+" : this.getMonth() + 1,
		"d+" : this.getDate(),
		"H+" : this.getHours(),
		"m+" : this.getMinutes(),
		"s+" : this.getSeconds(),
		"q+" : Math.floor((this.getMonth() + 3) / 3),
		"S+" : this.getMilliseconds()
	};
	if (/(y+)/i.test(format)) {
		format = format.replace(RegExp.$1, (this.getFullYear() + '')
				.substr(4 - RegExp.$1.length));
	}
	for ( var k in date) {
		if (new RegExp("(" + k + ")").test(format)) {
			format = format.replace(RegExp.$1, RegExp.$1.length == 1 ? date[k]
					: ("00" + date[k]).substr(("" + date[k]).length));
		}
	}
	return format;
}

function formatDate(now) {
	if (now == null || now == '') {
		return "";
	}

	return new Date(now).format("yyyy-MM-dd HH:mm:ss");
}

// 查看图片
function seePic(url) {
	window.open(url);
}

// 生成uuid
function S4() {
	return (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1);
}
function guid() {
	return (S4() + S4() + "-" + S4() + "-" + S4() + "-" + S4() + "-" + S4()
			+ S4() + S4());
}

// 时间字符串转时间戳
function strToTime(str) {
	var str = str.replace(/-/g, '/');
	var timestamp = new Date(str).getTime();
	
	return timestamp
}

// 获取url参数
function getQueryString(name) {
	var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
	var r = window.location.search.substr(1).match(reg);
	if (r != null)
		return unescape(r[2]);
	return null;
}

// 下载文件
function downloadFile(url, name) {
	window.open(ctx + "/downloadFile?url=" + encodeURIComponent(url) + "&name="
			+ encodeURIComponent(name));
}


// 显示载入框
var loadIndex;
function showLoad() {
	loadIndex = layer.load();
}
function closeLoad() {
	layer.close(loadIndex);
}

// 复制到剪切板
function copyToClipboard(text) {
    /*if(text.indexOf('-') !== -1) {
        let arr = text.split('-');
        text = arr[0] + arr[1];
    }*/
    var textArea = document.createElement("textarea");
      textArea.style.position = 'fixed';
      textArea.style.top = '0';
      textArea.style.left = '0';
      textArea.style.width = '2em';
      textArea.style.height = '2em';
      textArea.style.padding = '0';
      textArea.style.border = 'none';
      textArea.style.outline = 'none';
      textArea.style.boxShadow = 'none';
      textArea.style.background = 'transparent';
      textArea.value = text;
      document.body.appendChild(textArea);
      textArea.select();

      try {
        var successful = document.execCommand('copy');
        var msg = successful ? '成功复制到剪贴板' : '该浏览器不支持点击复制到剪贴板';
        layer.msg(msg);
      } catch (err) {
        layer.msg('该浏览器不支持点击复制到剪贴板');
      }

      document.body.removeChild(textArea);
}


// 规则对象(flag)
//var flag = new RegExp("[=~*:><^()]")

function hasSpec(str){
	if(str.indexOf("=") > -1 
	|| str.indexOf("*") > -1
	|| str.indexOf(":") > -1
	|| str.indexOf(";") > -1
	|| str.indexOf("~") > -1
	|| str.indexOf("\\") > -1
	|| str.indexOf("/") > -1
	|| str.indexOf("<") > -1
	|| str.indexOf(">") > -1
	|| str.indexOf("|") > -1
	|| str.indexOf("?") > -1
	|| str.indexOf("(") > -1
	|| str.indexOf(")") > -1
	|| str.indexOf("[") > -1
	|| str.indexOf("]") > -1
	|| str.indexOf("{") > -1
	|| str.indexOf("}") > -1
	|| str.indexOf("`") > -1
	|| str.indexOf("!") > -1
	|| str.indexOf("@") > -1
	|| str.indexOf("#") > -1
	|| str.indexOf("$") > -1
	|| str.indexOf("%") > -1
	|| str.indexOf("^") > -1
	|| str.indexOf("&") > -1
	|| str.indexOf("-") > -1
	|| str.indexOf("+") > -1
	){
		return true;
	}
	
	return false;
}
	