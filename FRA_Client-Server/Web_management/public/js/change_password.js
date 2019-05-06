$(document).ready(function () {

    $('#change_password').on('submit', function (e) {
        e.preventDefault()
        var formData = $(this).serialize()
        var Change = $.ajax({
            url: '/changePassword',
            type: 'post',
            data: formData,
            dataType: 'json',
            beforeSend: function (XMLHttpRequest) {
                if (($('#old_password').val() === '') || ($('#new_password').val() === '')) {
                    myAlert("密码不能为空！！", function () {
                    })
                    Change.abort()
                }
            },
            success: function (data) {
                var err_code = data.err_code
                if (err_code === 0) {
                    // 服务端重定向针对异步请求无效
                    myAlert("修改成功！</br>（马上退出,重新登录）", function () {
                        parent.window.location.href = '/login'
                    })
                } else if (err_code === 15) {
                    myAlert("密码不能为空！", function () {
                    })
                } else if (err_code === 1) {
                    myAlert("密码错误！！", function () {
                        window.location.href = '/changePassword'
                    })
                } else if (err_code === 500) {
                    myAlert("服务器忙，请稍后重试！", function () {
                    })
                }
            }
        })
    })

});

/*  自定义提示框样式  */
function myAlert(message, callback) {
    let d = dialog({
        title: '提示',
        content: message,
        width: 150,
        cancel: false,
        okValue: "确定",
        ok: callback
    });
    return d.showModal();
}


$(document).ready(function () {
    /*
     * 提交
     */
    $("#submitbutton").click(function () {
        if (validateForm()) {
            checkFyFhSubmit();
        }
    });

    /*
     * 取消
     */
    $("#cancelbutton").click(function () {
        /**  关闭弹出iframe  **/
        window.parent.$.fancybox.close();
    });

    var result = 'null';
    if (result == 'success') {
        /**  关闭弹出iframe  **/
        window.parent.$.fancybox.close();
    }
});

/** 检测房源房号是否存在  **/
function checkFyFh() {
    // 分别获取小区编号、栋号、层号、房号
    var fyID = $('#fyID').val();
    var fyXqCode = $("#fyXq").val();
    var fyDh = $("#fyDh").val();
    var fyCh = $("#fyCh").val();
    var fyFh = $("#fyFh").val();
    if (fyXqCode != "" && fyDh != "" && fyCh != "" && fyFh != "") {
        // 给房屋坐落地址赋值
        $("#fyZldz").val($('#fyDh option:selected').text() + fyCh + "-" + fyFh);
        // 异步判断该房室是否存在，如果已存在，给用户已提示哦
        $.ajax({
            type: "POST",
            url: "checkFyFhIsExists.action",
            data: {
                "fangyuanEntity.fyID": fyID,
                "fangyuanEntity.fyXqCode": fyXqCode,
                "fangyuanEntity.fyDhCode": fyDh,
                "fangyuanEntity.fyCh": fyCh,
                "fangyuanEntity.fyFh": fyFh
            },
            dataType: "text",
            success: function (data) {
// 					alert(data);
                // 如果返回数据不为空，更改“房源信息”
                if (data == "1") {
                    art.dialog({
                        icon: 'error',
                        title: '友情提示',
                        drag: false,
                        resize: false,
                        content: '该房室在系统中已经存在哦，\n请维护其他房室数据',
                        ok: true,
                    });
                    $("#fyFh").css("background", "#EEE");
                    $("#fyFh").focus();
                    return false;
                }
            }
        });
    }
}

/** 检测房源房号是否存在并提交form  **/
function checkFyFhSubmit() {
    // 分别获取小区编号、栋号、层号、房号
    var fyID = $('#fyID').val();
    var fyXqCode = $("#fyXq").val();
    var fyDh = $("#fyDh").val();
    var fyCh = $("#fyCh").val();
    var fyFh = $("#fyFh").val();
    if (fyXqCode != "" && fyDh != "" && fyCh != "" && fyFh != "") {
        // 给房屋坐落地址赋值
        $("#fyZldz").val($('#fyDh option:selected').text() + fyCh + "-" + fyFh);
        // 异步判断该房室是否存在，如果已存在，给用户已提示哦
        $.ajax({
            type: "POST",
            url: "checkFyFhIsExists.action",
            data: {
                "fangyuanEntity.fyID": fyID,
                "fangyuanEntity.fyXqCode": fyXqCode,
                "fangyuanEntity.fyDhCode": fyDh,
                "fangyuanEntity.fyCh": fyCh,
                "fangyuanEntity.fyFh": fyFh
            },
            dataType: "text",
            success: function (data) {
// 					alert(data);
                // 如果返回数据不为空，更改“房源信息”
                if (data == "1") {
                    art.dialog({
                        icon: 'error',
                        title: '友情提示',
                        drag: false,
                        resize: false,
                        content: '该房室在系统中已经存在哦，\n请维护其他房室数据',
                        ok: true,
                    });
                    $("#fyFh").css("background", "#EEE");
                    $("#fyFh").focus();
                    return false;
                } else {
                    $("#submitForm").attr("action", "/xngzf/archives/saveOrUpdateFangyuan.action").submit();
                }
            }
        });
    }
    return true;
}

/** 表单验证  **/
function validateForm() {
    if ($("#fyXqName").val() == "") {
        art.dialog({icon: 'error', title: '友情提示', drag: false, resize: false, content: '填写房源小区', ok: true,});
        return false;
    }
    if ($("#fyDh").val() == "") {
        art.dialog({icon: 'error', title: '友情提示', drag: false, resize: false, content: '填写房源栋号', ok: true,});
        return false;
    }
    if ($("#fyCh").val() == "") {
        art.dialog({icon: 'error', title: '友情提示', drag: false, resize: false, content: '填写房源层号', ok: true,});
        return false;
    }
    if ($("#fyFh").val() == "") {
        art.dialog({icon: 'error', title: '友情提示', drag: false, resize: false, content: '填写房源房号', ok: true,});
        return false;
    }
    if ($("#fyZongMj").val() == "") {
        art.dialog({icon: 'error', title: '友情提示', drag: false, resize: false, content: '填写房源面积', ok: true,});
        return false;
    }
    if ($("#fyJizuMj").val() == "") {
        art.dialog({icon: 'error', title: '友情提示', drag: false, resize: false, content: '填写计租面积', ok: true,});
        return false;
    }
    if ($("#fyZldz").val() == "") {
        art.dialog({icon: 'error', title: '友情提示', drag: false, resize: false, content: '填写房源座落地址', ok: true,});
        return false;
    }
    return true;
}