/*  修改数据后的同步刷新  */
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
                if (iframe_url[0] === 'http://' + window.location.host + '/main/user_list') {
                    parent.document.getElementById("rightMain").contentWindow.location.href = '/main/user_list' + '?' + iframe_url[1]
                }
            }
        }
    })

    socket.on('disconnect', function () {
        socket.close()
        console.log('client disconnect...')
        myAlert('与服务器断开连接<br>跳转至登录界面', function () {
            window.location.href = "/logout"
        })
    })

}

/*  退出系统  */
function logout() {
    myAlertWithCancel('退出当前系统？？', function () {
        window.location.href = '/logout'
    })
}

/*  获得当前日期  */
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


/* zTree插件加载目录的处理  */
var zTree;

var setting = {
    view: {
        dblClickExpand: false,
        showLine: false,
        expandSpeed: "fast"
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

/*  菜单跳转  */
function rightMain(url) {
    $('#rightMain').attr('src', url);
}


/* 用于捕获节点被点击的事件回调函数  */
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
    if (!treeNode.isParent) {
        $('#here_area_module').text(treeNode.getParentNode().resourceName)
        $('#here_area_submodule').text(treeNode.resourceName)
    }
};

/* 左边菜单切换 */
function switchTab(tabpage, tabid) {
    var oItem = document.getElementById(tabpage).getElementsByTagName("li");
    for (var i = 0; i < oItem.length; i++) {
        var x = oItem[i];
        x.className = "";
    }
    if ('left_tab1' == tabid) {
        $(document).ajaxStart(onStart).ajaxSuccess(onStop);
        // 异步加载"业务模块"下的菜单
        loadMenu('business_module', 'dleft_tab1');
    } else if ('left_tab2' == tabid) {
        $(document).ajaxStart(onStart).ajaxSuccess(onStop);
        // 异步加载"系统管理"下的菜单
        loadMenu('system_Management', 'dleft_tab1');
    } else if ('left_tab3' == tabid) {
        $(document).ajaxStart(onStart).ajaxSuccess(onStop);
        // 异步加载"其他"下的菜单
        loadMenu('other', 'dleft_tab1');
    }
}

$(document).ready(function () {
    $(document).ajaxStart(onStart).ajaxSuccess(onStop);
    /** 默认异步加载"业务模块"目录  **/
    loadMenu('business_module', "dleft_tab1");
    // 默认展开所有节点
    if (zTree) {
        // 默认展开所有节点
        zTree.expandAll(true);
    }

});


/* ************************************************
 下面是要异步加载模块树的内容。url是定义的服务器请求。data是服务器需要响应的一个json格式的数据。
 **************************************************** */
function loadMenu(resourceType, treeObj) {
    $.ajax({
        type: "GET",
        url: "/main/zTree?resourceType=" + resourceType,
        dataType: "json",
        success: function (data) {
            var err_code = data.err_code
            // 如果返回数据不为空，加载"业务模块"目录
            if (data != null) {
                // 将返回的数据赋给zTree
                $.fn.zTree.init($("#" + treeObj), setting, data);
                zTree = $.fn.zTree.getZTreeObj(treeObj);
                if (zTree) {
                    // 默认展开所有节点
                    zTree.expandAll(true);
                }
            } else {
                check_err(err_code)
            }
        }
    });

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

$(document).ready(function () {
    //  左部功能切换
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
        $('#nav_module').text(this.title)
        // 显示侧边栏
        switchSysBar(true);
    });

    // 显示隐藏侧边栏
    $("#show_hide_btn").click(function () {
        switchSysBar();
    });
})

/*  隐藏或者显示侧边栏  */
function switchSysBar(flag) {
    var side = $('#side');
    var left_menu_cnt = $('#left_menu_cnt');
    if (flag == true) {
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
