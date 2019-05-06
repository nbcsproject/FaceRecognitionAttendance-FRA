$(document).ready(function () {

    $('#register_form').on('submit', function (e) {
        e.preventDefault()
        var formData = $(this).serialize()

        var Register = $.ajax({
            url: '/register',
            type: 'post',
            data: formData,
            dataType: 'json',
            beforeSend: function (XMLHttpRequest) {
                if ($('#account').val() === '') {
                    myAlert("账号不能为空！！", function () {
                    })
                    Register.abort()
                } else if ($('#password').val() === '') {
                    myAlert("密码不能为空！！", function () {
                    })
                    Register.abort()
                }
            },
            success: function (data) {
                var err_code = data.err_code
                if (err_code === 0) {
                    // 服务端重定向针对异步请求无效
                    myAlert("注册成功！！", function () {
                        window.location.href = '/login'
                    })
                } else if (err_code === 10) {
                    myAlert("账号不能为空！", function () {
                    })
                } else if (err_code === 15) {
                    myAlert("密码不能为空！", function () {
                    })
                } else if (err_code === 1) {
                    myAlert("账号已存在！", function () {
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