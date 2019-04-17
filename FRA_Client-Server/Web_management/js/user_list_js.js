$(document).ready(function () {
	/** 新增   **/
	$("#addBtn").fancybox({
		'href': 'user_edit.html',
		'width': 733,
		'height': 530,
		'type': 'iframe',
		'hideOnOverlayClick': false,
		'showCloseButton': false,
		'onClosed': function () {
			window.location.href = 'user_list.html';
		}
	});

	/** 导入  **/
	$("#importBtn").fancybox({
		'href': '/xngzf/archives/importFangyuan.action',
		'width': 633,
		'height': 260,
		'type': 'iframe',
		'hideOnOverlayClick': false,
		'showCloseButton': false,
		'onClosed': function () {
			window.location.href = 'user_list.html';
		}
	});

	/**编辑   **/
	$("a.edit").fancybox({
		'width': 733,
		'height': 530,
		'type': 'iframe',
		'hideOnOverlayClick': false,
		'showCloseButton': false,
		'onClosed': function () {
			window.location.href = 'user_list.html';
		}
	});
});
/** 用户角色   **/
var userRole = '';

/** 模糊查询来电用户  **/
function search() {
	$("#submitForm").attr("action", "house_list.html?page=" + 1).submit();
}

/** 新增   **/
function add() {
	$("#submitForm").attr("action", "/xngzf/archives/luruFangyuan.action").submit();
}

/** Excel导出  **/
function exportExcel() {
	if (confirm('您确定要导出吗？')) {
		var fyXqCode = $("#fyXq").val();
		var fyXqName = $('#fyXq option:selected').text();
//	 		alert(fyXqCode);
		if (fyXqCode == "" || fyXqCode == null) {
			$("#fyXqName").val("");
		} else {
//	 			alert(fyXqCode);
			$("#fyXqName").val(fyXqName);
		}
		$("#submitForm").attr("action", "/xngzf/archives/exportExcelFangyuan.action").submit();
	}
}

/** 删除 **/
function del(fyID) {
	// 非空判断
	if (fyID == '') return;
	if (confirm("您确定要删除吗？")) {
		$("#submitForm").attr("action", "/xngzf/archives/delFangyuan.action?fyID=" + fyID).submit();
	}
}

/** 批量删除 **/
function batchDel() {
	if ($("input[name='IDCheck']:checked").size() <= 0) {
		var d = dialog({
			title: '温馨提示',
			content: '请至少选择一条',
			width: 150,
			cancel: false,
			ok: function () {
			}
		});
		d.show();
		return;
	}
	// 1）取出用户选中的checkbox放入字符串传给后台,form提交
	var allIDCheck = "";
	$("input[name='IDCheck']:checked").each(function (index, domEle) {
		bjText = $(domEle).parent("td").parent("tr").last().children("td").last().prev().text();
// 			alert(bjText);
		// 用户选择的checkbox, 过滤掉“已审核”的，记住哦
		if ($.trim(bjText) == "已审核") {
// 				$(domEle).removeAttr("checked");
			$(domEle).parent("td").parent("tr").css({color: "red"});
			$("#resultInfo").html("已审核的是不允许您删除的，请联系管理员删除！！！");
// 				return;
		} else {
			allIDCheck += $(domEle).val() + ",";
		}
	});
	// 截掉最后一个","
	if (allIDCheck.length > 0) {
		allIDCheck = allIDCheck.substring(0, allIDCheck.length - 1);
		// 赋给隐藏域
		$("#allIDCheck").val(allIDCheck);
		if (confirm("您确定要批量删除这些记录吗？")) {
			// 提交form
			$("#submitForm").attr("action", "/xngzf/archives/batchDelFangyuan.action").submit();
		}
	}
}

/** 普通跳转 **/
function jumpNormalPage(page) {
	$("#submitForm").attr("action", "house_list.html?page=" + page).submit();
}

/** 输入页跳转 **/
function jumpInputPage(totalPage) {
	// 如果“跳转页数”不为空
	if ($("#jumpNumTxt").val() != '') {
		var pageNum = parseInt($("#jumpNumTxt").val());
		// 如果跳转页数在不合理范围内，则置为1
		if (pageNum < 1 | pageNum > totalPage) {
			var d = dialog({
				title: '温馨提示',
				content: '请输入合适的页数，\n自动为您跳到首页',
				resize: false,
				cancel: false,
				ok: function () {
				}
			});
			d.show();
			pageNum = 1;
		}
		$("#submitForm").attr("action", "house_list.html?page=" + pageNum).submit();
	} else {
		// “跳转页数”为空
		var d = dialog({
			title: '温馨提示',
			content: '请输入合适的页数，\n自动为您跳到首页',
			resize: false,
			cancel: false,
			ok: function () {
			}
		});
		d.show();
		$("#submitForm").attr("action", "house_list.html?page=" + 1).submit();
	}
}