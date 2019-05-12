/*----table鼠标悬停换色-------*/
$(document).ready(function () {
    // 如果鼠标移到行上时，执行函数

    $(".table tr").mouseover(function () {
        $(this).css({background: "#CDDAEB"});
        $(this).children('td').each(function (index, ele) {
            $(ele).css({color: "#1D1E21"});
        });
    }).mouseout(function () {
        $(this).css({background: "#FFF"});
        $(this).children('td').each(function (index, ele) {
            $(ele).css({color: "#909090"});
        });
    })


});

/*---------错误编号-检查-----------*/

function check_err(err_code) {
    switch (err_code) {
        case 3306:
            myAlert("服务器正在维护，请稍后再试！！", function () {
            })
            break;
        case 3300:
            myAlert("服务器正在维护，请稍后再试！！", function () {
            })
            break;
        case 10:
            myAlert("账号不能为空！！", function () {
            })
            break;
        case 11:
            myAlert("密码不能为空！！", function () {
            })
            break;
        case 12:
            myAlert("账号或者密码错误！！", function () {
            })
            break;
        case 13:
            myAlert("账号已存在！！", function () {
            })
            break;
        case 20:
            myAlert("删除失败，请稍后再试！！", function () {
            })
            break;
    }
}

/*  自定义提示框样式  */
function myAlert(message, callback) {
    let d = dialog({
        title: '提示',
        content: message,
        width: 200,
        cancel: false,
        okValue: "确定",
        ok: callback
    });
    return d.showModal();
}

function myAlertWithCancel(message, callbak) {
    let d = dialog({
        title: '提示',
        content: message,
        width: 200,
        cancel: function () {
        },
        cancelValue: "取消",
        ok: callbak,
        okValue: "确定",
    });
    return d.showModal();
}