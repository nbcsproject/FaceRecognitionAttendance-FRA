function socket() {

    var socket = io('http://10.10.19.134:3001')
    socket.on('connect', function () {
        console.log('client connect server...')
    })
    socket.on('event', function (data) {
        if (data.msg === 'ok') {
            var iframe_url = parent.document.getElementById("rightMain").contentWindow.location.href
            iframe_url = iframe_url.split('?')
            if (iframe_url.length > 1) {
                if (iframe_url[0] === 'http://10.10.19.134:3000/user_list') {
                    parent.document.getElementById("rightMain").contentWindow.location.href = '/user_list' + '?' + iframe_url[1]
                }
            }
        }
    })

    socket.on('disconnect', function () {
        console.log('client disconnect')
    })

}


/**退出系统**/
function logout() {
    var d = dialog({
        title: '提示',
        content: '退出当前系统？？',
        okValue: '确定',
        ok: function () {
            window.location.href = '/logout';
        },
        cancelValue: '取消',
        cancel: function () {
        }
    });
    d.showModal()
}

/**获得当前日期**/
function getDate01() {
    var time = new Date();
    var myYear = time.getFullYear();
    var myMonth = time.getMonth() + 1;
    var myDay = time.getDate();
    var weekDay = new Array(7);
    weekDay[0] = "星期日";
    weekDay[1] = "星期一";
    weekDay[2] = "星期二";
    weekDay[3] = "星期三";
    weekDay[4] = "星期四";
    weekDay[5] = "星期五";
    weekDay[6] = "星期六";

    if (myMonth < 10) {
        myMonth = "0" + myMonth;
    }
    if (myDay < 10) {
        myDay = "0" + myDay;
    }
    document.getElementById("day_day").innerHTML = myYear + " . " + myMonth + " . " + myDay + "&nbsp;&nbsp;&nbsp;" + weekDay[time.getDay()];
}

/**加入收藏夹**/
function addfavorite() {
    var ua = navigator.userAgent.toLowerCase();
    if (ua.indexOf("360se") > -1) {
        art.dialog({
            icon: 'error',
            title: '友情提示',
            drag: false,
            resize: false,
            content: '由于360浏览器功能限制，加入收藏夹功能失效',
            ok: true,
        });
    } else if (ua.indexOf("msie 8") > -1) {
        window.external.AddToFavoritesBar('${dynamicURL}/authority/loginInit.action', 'FRA管理系统');//IE8
    } else if (document.all) {
        window.external.addFavorite('${dynamicURL}/authority/loginInit.action', 'FRA管理系统');
    } else {
        art.dialog({
            icon: 'error',
            title: '友情提示',
            drag: false,
            resize: false,
            content: '添加失败，请用ctrl+D进行添加',
            ok: true,
        });
    }
}

/* zTree插件加载目录的处理  */
var zTree;

var setting = {
    view: {
        dblClickExpand: false,
        showLine: false,
        expandSpeed: ($.browser.msie && parseInt($.browser.version) <= 6) ? "" : "fast"
    },
    data: {
        key: {
            name: "resourceName"
        },
        simpleData: {
            enable: true,
            idKey: "resourceID",
            pIdKey: "parentID",
            rootPId: ""
        }
    },
    callback: {
        beforeExpand: beforeExpand,
        onExpand: onExpand,
        onClick: zTreeOnClick
    }
};

var curExpandNode = null;

function beforeExpand(treeId, treeNode) {
    var pNode = curExpandNode ? curExpandNode.getParentNode() : null;
    var treeNodeP = treeNode.parentTId ? treeNode.getParentNode() : null;
    for (var i = 0, l = !treeNodeP ? 0 : treeNodeP.children.length; i < l; i++) {
        if (treeNode !== treeNodeP.children[i]) {
            zTree.expandNode(treeNodeP.children[i], false);
        }
    }
    while (pNode) {
        if (pNode === treeNode) {
            break;
        }
        pNode = pNode.getParentNode();
    }
    if (!pNode) {
        singlePath(treeNode);
    }

}

function singlePath(newNode) {
    if (newNode === curExpandNode) return;
    if (curExpandNode && curExpandNode.open == true) {
        if (newNode.parentTId === curExpandNode.parentTId) {
            zTree.expandNode(curExpandNode, false);
        } else {
            var newParents = [];
            while (newNode) {
                newNode = newNode.getParentNode();
                if (newNode === curExpandNode) {
                    newParents = null;
                    break;
                } else if (newNode) {
                    newParents.push(newNode);
                }
            }
            if (newParents != null) {
                var oldNode = curExpandNode;
                var oldParents = [];
                while (oldNode) {
                    oldNode = oldNode.getParentNode();
                    if (oldNode) {
                        oldParents.push(oldNode);
                    }
                }
                if (newParents.length > 0) {
                    for (var i = Math.min(newParents.length, oldParents.length) - 1; i >= 0; i--) {
                        if (newParents[i] !== oldParents[i]) {
                            zTree.expandNode(oldParents[i], false);
                            break;
                        }
                    }
                } else {
                    zTree.expandNode(oldParents[oldParents.length - 1], false);
                }
            }
        }
    }
    curExpandNode = newNode;
}

function onExpand(event, treeId, treeNode) {
    curExpandNode = treeNode;
}

/** 用于捕获节点被点击的事件回调函数  **/
function zTreeOnClick(event, treeId, treeNode) {
    var zTree = $.fn.zTree.getZTreeObj("dleft_tab1");
    zTree.expandNode(treeNode, null, null, null, true);
    // 		zTree.expandNode(treeNode);
    // 规定：如果是父类节点，不允许单击操作
    if (treeNode.isParent) {
        // 			alert("父类节点无法点击哦...");
        return false;
    }
    // 如果节点路径为空或者为"#"，不允许单击操作
    if (treeNode.accessPath == "" || treeNode.accessPath == "#") {
        //alert("节点路径为空或者为'#'哦...");
        return false;
    }
    // 跳到该节点下对应的路径, 把当前资源ID(resourceID)传到后台，写进Session
    rightMain(treeNode.accessPath);

    if (treeNode.isParent) {
        $('#here_area').html('当前位置：' + treeNode.getParentNode().resourceName + '&nbsp;>&nbsp;<span style="color:#1A5CC6">' + treeNode.resourceName + '</span>');
    } else {
        $('#here_area').html('当前位置：系统&nbsp;>&nbsp;<span style="color:#1A5CC6">' + treeNode.resourceName + '</span>');
    }
}
;

/* 上方菜单 */
function switchTab(tabpage, tabid) {
    var oItem = document.getElementById(tabpage).getElementsByTagName("li");
    for (var i = 0; i < oItem.length; i++) {
        var x = oItem[i];
        x.className = "";
    }
    if ('left_tab1' == tabid) {
        $(document).ajaxStart(onStart).ajaxSuccess(onStop);
        // 异步加载"业务模块"下的菜单
        loadMenu('YEWUMOKUAI', 'dleft_tab1');
    } else if ('left_tab2' == tabid) {
        $(document).ajaxStart(onStart).ajaxSuccess(onStop);
        // 异步加载"系统管理"下的菜单
        loadMenu('XITONGMOKUAI', 'dleft_tab1');
    } else if ('left_tab3' == tabid) {
        $(document).ajaxStart(onStart).ajaxSuccess(onStop);
        // 异步加载"其他"下的菜单
        loadMenu('QITAMOKUAI', 'dleft_tab1');
    }
}

$(document).ready(function () {
    $(document).ajaxStart(onStart).ajaxSuccess(onStop);
    /** 默认异步加载"业务模块"目录  **/
    loadMenu('YEWUMOKUAI', "dleft_tab1");
    // 默认展开所有节点
    if (zTree) {
        // 默认展开所有节点
        zTree.expandAll(true);
    }

});


/********************************
 下面是要异步加载模块树的内容。url是定义的服务器请求。data是服务器需要响应的一个json格式的数据。
 ************************/
function loadMenu(resourceType, treeObj) {
    $.ajax({
        type: "POST",
        url: "${dynamicURL}/authority/modelPart.action?resourceType=" + resourceType,
        dataType: "json",
        success: function (data) {
            // 如果返回数据不为空，加载"业务模块"目录
            if (data != null) {
                // 将返回的数据赋给zTree
                $.fn.zTree.init($("#" + treeObj), setting, data);
                alert(treeObj);
                zTree = $.fn.zTree.getZTreeObj(treeObj);
                if (zTree) {
                    // 默认展开所有节点
                    zTree.expandAll(true);
                }
            }
        }
    });
    data = [{
        "accessPath": "",
        "checked": false,
        "delFlag": 0,
        "parentID": 1,
        "resourceCode": "",
        "resourceDesc": "",
        "resourceGrade": 2,
        "resourceID": 3,
        "resourceName": "基础数据",
        "resourceOrder": 0,
        "resourceType": ""
    },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 37,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 2,
            "resourceID": 19,
            "resourceName": "出租方设置",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 37,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 2,
            "resourceID": 20,
            "resourceName": "租金评定设置",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 1,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 2,
            "resourceID": 2,
            "resourceName": "摇号配租",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 1,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 2,
            "resourceID": 16,
            "resourceName": "签约入住",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 1,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 2,
            "resourceID": 24,
            "resourceName": "公租收费",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 1,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 2,
            "resourceID": 34,
            "resourceName": "日常业务",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 1,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 2,
            "resourceID": 55,
            "resourceName": "解约退租",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 1,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 2,
            "resourceID": 30,
            "resourceName": "统计报表",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 1,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 2,
            "resourceID": 37,
            "resourceName": "系统参数",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 30,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 3,
            "resourceID": 31,
            "resourceName": "公租房楼盘表概况",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 55,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 3,
            "resourceID": 44,
            "resourceName": "退租申请",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 24,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 3,
            "resourceID": 26,
            "resourceName": "日常合同扣租",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 16,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 3,
            "resourceID": 17,
            "resourceName": "通知书审核",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "user_list",
            "checked": false,
            "delFlag": 0,
            "parentID": 3,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 3,
            "resourceID": 7,
            "resourceName": "房源管理",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "loupanchart",
            "checked": false,
            "delFlag": 0,
            "parentID": 3,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 3,
            "resourceID": 8,
            "resourceName": "承租方管理",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 2,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 3,
            "resourceID": 5,
            "resourceName": "房源导出",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 16,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 3,
            "resourceID": 18,
            "resourceName": "电子合同管理",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 24,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 3,
            "resourceID": 59,
            "resourceName": "日常合同续费",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 34,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 3,
            "resourceID": 35,
            "resourceName": "房屋资产盘查",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 55,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 3,
            "resourceID": 45,
            "resourceName": "物业结算",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 30,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 3,
            "resourceID": 46,
            "resourceName": "公租房租赁变动概况",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 30,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 3,
            "resourceID": 47,
            "resourceName": "公租房增减变动情况",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 55,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 3,
            "resourceID": 43,
            "resourceName": "退租验房",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 34,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 3,
            "resourceID": 36,
            "resourceName": "园区企业盘查",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 24,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 3,
            "resourceID": 42,
            "resourceName": "维修费用审核",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 16,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 3,
            "resourceID": 33,
            "resourceName": "租金核算",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 2,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 3,
            "resourceID": 6,
            "resourceName": "租户导出",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 3,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 3,
            "resourceID": 32,
            "resourceName": "家庭成员管理",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 3,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 3,
            "resourceID": 39,
            "resourceName": "施工单位管理",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 2,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 3,
            "resourceID": 9,
            "resourceName": "配租导入",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 16,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 3,
            "resourceID": 52,
            "resourceName": "租金缴费",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 24,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 3,
            "resourceID": 25,
            "resourceName": "租金费用调整",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 34,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 3,
            "resourceID": 51,
            "resourceName": "园区企业查询",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 55,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 3,
            "resourceID": 29,
            "resourceName": "清账核算",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 30,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 3,
            "resourceID": 50,
            "resourceName": "公租房租金收入概况",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 37,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 3,
            "resourceID": 38,
            "resourceName": "维修单位设置",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 30,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 3,
            "resourceID": 48,
            "resourceName": "个人缴费记录",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 24,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 3,
            "resourceID": 53,
            "resourceName": "维修费用报销",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 16,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 3,
            "resourceID": 40,
            "resourceName": "入住验房",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 16,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 3,
            "resourceID": 41,
            "resourceName": "入住确认",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 24,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 3,
            "resourceID": 56,
            "resourceName": "租金调整审核",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 30,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 3,
            "resourceID": 49,
            "resourceName": "房租缴纳明细",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 55,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 3,
            "resourceID": 28,
            "resourceName": "合同解约",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 55,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 3,
            "resourceID": 54,
            "resourceName": "财务清账",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 16,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 3,
            "resourceID": 60,
            "resourceName": "合同续订",
            "resourceOrder": 0,
            "resourceType": ""
        },
        {
            "accessPath": "",
            "checked": false,
            "delFlag": 0,
            "parentID": 16,
            "resourceCode": "",
            "resourceDesc": "",
            "resourceGrade": 3,
            "resourceID": 58,
            "resourceName": "合同查询",
            "resourceOrder": 0,
            "resourceType": ""
        }];
    // 如果返回数据不为空，加载"业务模块"目录
    if (data != null) {
        // 将返回的数据赋给zTree
        $.fn.zTree.init($("#" + treeObj), setting, data);
//              alert(treeObj);
        zTree = $.fn.zTree.getZTreeObj(treeObj);
        if (zTree) {
            // 默认展开所有节点
            zTree.expandAll(true);
        }
    }
}

//ajax start function
function onStart() {
    $("#ajaxDialog").show();
}

//ajax stop function
function onStop() {
    // 		$("#ajaxDialog").dialog("close");
    $("#ajaxDialog").hide();
}

$(function () {
    $('#TabPage2 li').click(function () {
        var index = $(this).index();
        $(this).find('img').attr('src', './public/img/common/' + (index + 1) + '_hover.png');
        $(this).css({background: '#fff'});
        $('#nav_module').find('img').attr('src', './publicimg/common/module_' + (index + 1) + '.png');
        $('#TabPage2 li').each(function (i, ele) {
            if (i != index) {
                $(ele).find('img').attr('src', './public/img/common/' + (i + 1) + '.png');
                $(ele).css({background: '#044599'});
            }
        });
        // 显示侧边栏
        switchSysBar(true);
    });

    // 显示隐藏侧边栏
    $("#show_hide_btn").click(function () {
        switchSysBar();
    });
});

/**隐藏或者显示侧边栏**/
function switchSysBar(flag) {
    var side = $('#side');
    var left_menu_cnt = $('#left_menu_cnt');
    if (flag == true) {	// flag==true
        left_menu_cnt.show(500, 'linear');
        side.css({width: '280px'});
        $('#top_nav').css({width: '77%', left: '304px'});
        $('#main').css({left: '280px'});
    } else {
        if (left_menu_cnt.is(":visible")) {
            left_menu_cnt.hide(10, 'linear');
            side.css({width: '60px'});
            $('#top_nav').css({width: '100%', left: '60px', 'padding-left': '28px'});
            $('#main').css({left: '60px'});
            $("#show_hide_btn").find('img').attr('src', './public/img/common/nav_show.png');
        } else {
            left_menu_cnt.show(500, 'linear');
            side.css({width: '280px'});
            $('#top_nav').css({width: '77%', left: '304px', 'padding-left': '0px'});
            $('#main').css({left: '280px'});
            $("#show_hide_btn").find('img').attr('src', './public/img/common/nav_hide.png');
        }
    }
}
