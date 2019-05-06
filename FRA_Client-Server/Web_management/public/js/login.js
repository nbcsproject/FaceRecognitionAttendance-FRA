$(document).ready(function () {

    $('#login_form').on('submit', function (e) {
        e.preventDefault()
        var formData = $(this).serialize()

        var Login = $.ajax({
            url: '/login',
            type: 'post',
            data: formData,
            dataType: 'json',
            beforeSend: function (XMLHttpRequest) {
                if ($('#account').val() === '') {
                    myAlert("账号不能为空！！", function () {
                    })
                    Login.abort()
                } else if ($('#password').val() === '') {
                    myAlert("密码不能为空！！", function () {
                    })
                    Login.abort()
                }
            },
            success: function (data) {
                var err_code = data.err_code
                if (err_code === 0) {
                    // 服务端重定向针对异步请求无效
                    window.location.href = '/'
                } else if (err_code === 10) {
                    myAlert("账号不能为空！", function () {
                    })
                } else if (err_code === 15) {
                    myAlert("密码不能为空！", function () {
                    })
                } else if (err_code === 1) {
                    myAlert("账号或者密码错误！！", function () {
                    })
                } else if (err_code === 500) {
                    myAlert("服务器忙，请稍后重试！", function () {
                    })
                }
            }
        })
    })

    $("#register").click(function () {
        window.location.href = '/register'
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
        ok: callback,
    });
    return d.showModal();
}