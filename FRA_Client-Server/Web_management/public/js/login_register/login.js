$(document).ready(function () {

    $('#login_form').on('submit', function (e) {
        e.preventDefault()
        var formData = $(this).serialize()

        $.ajax({
            url: '/login',
            type: 'post',
            data: formData,
            dataType: 'json',
            beforeSend: function (XMLHttpRequest) {
                if ($('#account').val() === '') {
                    myAlert("账号不能为空！！", function () {
                    })
                    XMLHttpRequest.abort()
                } else if ($('#password').val() === '') {
                    myAlert("密码不能为空！！", function () {
                    })
                    XMLHttpRequest.abort()
                }
            },
            success: function (data) {
                var err_code = data.err_code
                if (err_code === 0) {
                    window.location.href = '/'
                } else {
                    check_err(err_code)
                }
            }
        })
    })

    $("#register").click(function () {
        window.location.href = '/register'
    })
});
