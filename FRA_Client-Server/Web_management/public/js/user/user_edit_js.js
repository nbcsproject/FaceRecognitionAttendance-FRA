$(document).ready(function () {

    /*    取消     */
    $("#cancelbutton").click(function () {
        /*  关闭弹出iframe  */
        parent.window.$.fancybox.close();
    });

    $('#user_edit').submit(function (e) {
        e.preventDefault()
        var formData = $(this).serialize()
        $.ajax({
            url: '/main/user_edit',
            type: 'post',
            data: formData,
            dataType: 'json',
            success: function (data) {
                var err_code = data.err_code
                if (err_code === 0) {
                    myAlert('修改成功！！', function () {
                        parent.window.$.fancybox.close()
                        parent.window.location.reload()
                    })
                } else {
                    check_err(err_code)
                }
            }
        })
    })
});