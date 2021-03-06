$(document).ready(function () {

    $('#change_password').on('submit', function (e) {
        e.preventDefault()
        var formData = $(this).serialize()
        $.ajax({
            url: '/changePassword',
            type: 'post',
            data: formData,
            dataType: 'json',
            beforeSend: function (XMLHttpRequest) {
                if (($('#old_password').val() === '') || ($('#new_password').val() === '')) {
                    myAlert("密码不能为空！！", function () {
                    })
                    XMLHttpRequest.abort()
                }
            },
            success: function (data) {
                var err_code = data.err_code
                if (err_code === 0) {
                    myAlert("修改成功！</br>（马上退出,重新登录）", function () {
                        console.log(parent.window.location.href)
                        parent.window.location.href = '/logout'
                    })
                } else {
                    check_err(err_code)
                }
            }
        })
    })
});
