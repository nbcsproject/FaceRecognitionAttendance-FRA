$(document).ready(function () {
/*

    var socket = io('http://10.10.19.134:3001')
    socket.on('connect', function () {
        console.log('client connect server...')
    })
    socket.on('event', function (data) {
        if (data.msg === 'ok') {

            window.location.href = '/user_list'
        }
    })

    socket.on('disconnect', function () {
        console.log('client disconnect')
    })

*/

    /*  全选框操作  */
    $(function () {
        var all_checked = false;
        $(":checkbox").click(function () {
            var table = $(this).parents("table");
            if ($(this).attr("id") === "all") {
                table.find(":checkbox").prop("checked", !all_checked);
                all_checked = !all_checked;
            } else {
                table.find(":checkbox[id!=all]").each(function (i) {
                    if (!$(this).is(":checked")) {
                        table.find("#all").prop("checked", false);
                        all_checked = false;
                        return false;
                    }
                    $("#all").prop("checked", true);
                    all_checked = true;
                });
            }
        });
    });
    /** 新增   **/
    $("#addBtn").fancybox({
        'href': '/user_new',
        'width': 733,
        'height': 530,
        'type': 'iframe',
        'hideOnOverlayClick': false,
        'showCloseButton': false,
        'onClosed': function () {
            window.location.href = '/user_list';
        }
    });
    /*    $(".editBtn").fancybox({
            'href': '/user_edit',
            'width': 733,
            'height': 530,
            'type': 'iframe',
            'hideOnOverlayClick': false,
            'showCloseButton': false,
            'onClosed': function () {
                window.location.href = '/user_list';
            }
        });*/
    /** 导入  **/
    $("#importBtn").fancybox({
        'href': '/xngzf/archives/importFangyuan.action',
        'width': 633,
        'height': 260,
        'type': 'iframe',
        'hideOnOverlayClick': false,
        'showCloseButton': false,
        'onClosed': function () {
            window.location.href = '/user_list';
        }
    });

    /*  编辑   */
    $("a.edit").fancybox({
        'width': 733,
        'height': 530,
        'type': 'iframe',
        'hideOnOverlayClick': false,
        'showCloseButton': false,
        'onClosed': function () {
            window.location.href = '/user_list';
        }
    });
    /*    分页操作    */
    /*  跳转首页  */
    $("#firstPage").click(function () {
        let currentPage = parseInt($("#pages_currentPage").text())
        if (currentPage === 1) {
            myAlert("当前已是首页！！")
        } else {
            // 如果不是首页跳转到首页
            window.location.href = '/user_list?page=1';
        }
    });
    /* 上一页  */
    $("#prePage").click(function () {
        let currentPage = parseInt($("#pages_currentPage").text())
        if (currentPage === 1) {
            myAlert("当前已是首页！！")
        } else {
            // 如果不是首页跳转到首页
            window.location.href = '/user_list?page=' + (currentPage - 1);
        }
    });
    /*  下一页  */
    $("#nextPage").click(function () {
        let currentPage = parseInt($("#pages_currentPage").text())
        let totalPage = parseInt($("#pages_totalPage").text())
        if (currentPage === totalPage) {
            myAlert("当前已是尾页！！")
        } else {
            // 如果不是尾页跳转到尾页
            window.location.href = '/user_list?page=' + (currentPage + 1);
        }
    })
    /*  跳转尾页  */
    $("#lastPage").click(function () {
        let currentPage = parseInt($("#pages_currentPage").text())
        let totalPage = parseInt($("#pages_totalPage").text())
        if (currentPage === totalPage) {
            myAlert("当前已是尾页！！")
        } else {
            // 如果不是尾页跳转到尾页
            window.location.href = '/user_list?page=' + totalPage;
        }
    });
    /*  跳转指定页面  */
    $("#jumpInputPage").click(function () {
        let totalPage = parseInt($("#pages_totalPage").text())
        let currentPage = parseInt(($("#pages_currentPage").text()))
        let inputPage = parseInt($("#jumpNumTxt").val())
        if (!isNaN(inputPage)) {
            if (inputPage < 1 || inputPage > totalPage) {
                myAlert("请输入合适的页数！！")
                $("#jumpNumTxt").attr("value", '')
            } else if (inputPage === currentPage) {
                if (inputPage === 1) {
                    myAlert("当前已是首页！！")
                } else if (inputPage === totalPage) {
                    myAlert("当前已是尾页！！")
                } else {
                    myAlert("已是当前页！！")
                }
            } else {
                // 如果不是首、尾页跳转到指定页
                window.location.href = '/user_list?page=' + inputPage;
            }
        } else {
            myAlert("请输入合适的页数！！")
            $("#jumpNumTxt").attr("value", '')
        }

    });

});
/** 用户角色   **/
var userRole = '';

/** 模糊查询来电用户  **/
function search() {
    // $("#submitForm").attr("action", "house_list.html?page=" + 1).submit();
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

/*  自定义提示框样式  */
function myAlert(message) {
    let d = dialog({
        title: '提示',
        content: message,
        width: 150,
        cancel: false,
        okValue: "确定",
        ok: function () {
        }
    });
    return d.showModal();
}