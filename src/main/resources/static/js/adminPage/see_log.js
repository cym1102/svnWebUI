/*var svnCommitLogList;
function seeLog(url) {

	layer.open({
		type: 1,
		title: "提交日志",
		area: ['800px', '600px'], // 宽高
		content: $('#seeLogDiv')
	});

	showLoad();
	$.ajax({
		type: 'POST',
		url: ctx + '/adminPage/seeLog/seeLog',
		data: {
			url: url
		},
		dataType: 'json',
		success: function(data) {
			closeLoad();
			if (data.success) {
				svnCommitLogList = data.obj;
				var html = "";
				for (let i = 0; i < svnCommitLogList.length; i++) {
					var svnCommitLog = svnCommitLogList[i];

					html += `
						<tr>
							<td>
								${svnCommitLog.revision}
							</td>
							<td>
								${svnCommitLog.author}
							</td>
							<td>
								${svnCommitLog.commitDate}
							</td>
							<td>
								${svnCommitLog.message}
							</td>
							<td>
								<a class="blue" href="javascript:seeLogFile(${svnCommitLog.revision})">查看</a>
							</td>
						</tr>
					`
				}

				$("#logTable").html(html);
			} else {
				layer.msg(data.msg);
			}
		},
		error: function() {
			layer.alert("出错了,请联系技术人员!");
		}
	});

}*/


function seeLogFile(revision) {
	/*var svnCommitLog = null;
	for (let i = 0; i < svnCommitLogList.length; i++) {
		if (svnCommitLogList[i].revision == revision) {
			svnCommitLog = svnCommitLogList[i];
			break;
		}
	}

	var log = ``;
	for (let i = 0; i < svnCommitLog.fileList.length; i++) {
		var svnFileEntry = svnCommitLog.fileList[i];
		log += `<div class="file_${svnFileEntry.type}">${svnFileEntry.type}&nbsp;&nbsp;&nbsp;${svnFileEntry.path}</div>`;
	}
	
	$("#seeLogFileDiv").html(log);*/
	
	layer.open({
		type: 1,
		title: "文件变化",
		area: ['800px', '90%'], // 宽高
		content: $('#seeLogFileDiv_' + revision)
	});
}