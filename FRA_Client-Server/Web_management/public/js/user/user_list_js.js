$(document).ready(function () {
    const userList_url = '/main/user_list?byPage=true&'

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

    /*--------小窗口显示-----------*/
    $(".fancybox").fancybox({
        'width': 733,
        'height': 530,
        'type': 'iframe',
        'showCloseButton': false,
        'onClosed': function () {
        }
    });

    /*---显示全部按钮----*/
    $('#showAll').click(function () {
        window.location.href = userList_url + 'page=1'
    })

    /*----------- ---分页操作--------------------*/

    /*  跳转首页  */
    $("#firstPage").click(function () {
        let currentPage = parseInt($("#pages_currentPage").text())
        if (currentPage === 1) {
            myAlert("当前已是首页！！", function () {
            })
        } else {
            // 如果不是首页跳转到首页
            window.location.href = userList_url + 'page=1&searchText=' + getSearchText();
        }
    });

    /* 上一页  */
    $("#prePage").click(function () {
        let currentPage = parseInt($("#pages_currentPage").text())
        if (currentPage === 1) {
            myAlert("当前已是首页！！", function () {
            })
        } else {
            // 如果不是首页跳转到首页
            window.location.href = userList_url + 'page=' + (currentPage - 1) + '&searchText=' + getSearchText();
        }
    });

    /*  下一页  */
    $("#nextPage").click(function () {
        let currentPage = parseInt($("#pages_currentPage").text())
        let totalPage = parseInt($("#pages_totalPage").text())
        if (currentPage === totalPage) {
            myAlert("当前已是尾页！！", function () {
            })
        } else {
            // 如果不是尾页跳转到尾页
            window.location.href = userList_url + 'page=' + (currentPage + 1) + '&searchText=' + getSearchText();
        }
    })

    /*  跳转尾页  */
    $("#lastPage").click(function () {
        let currentPage = parseInt($("#pages_currentPage").text())
        let totalPage = parseInt($("#pages_totalPage").text())
        if (currentPage === totalPage) {
            myAlert("当前已是尾页！！", function () {
            })
        } else {
            // 如果不是尾页跳转到尾页
            window.location.href = userList_url + 'page=' + totalPage + '&searchText=' + getSearchText();
        }
    });

    /*  跳转指定页面  */
    $("#jumpInputPage").click(function () {
        let totalPage = parseInt($("#pages_totalPage").text())
        let currentPage = parseInt(($("#pages_currentPage").text()))
        let inputPage = parseInt($("#jumpNumTxt").val())
        if (!isNaN(inputPage)) {
            if (inputPage < 1 || inputPage > totalPage) {
                myAlert("请输入合适的页数！！", function () {
                })
                $("#jumpNumTxt").attr("value", '')
            } else if (inputPage === currentPage) {
                if (inputPage === 1) {
                    myAlert("当前已是首页！！", function () {
                    })
                } else if (inputPage === totalPage) {
                    myAlert("当前已是尾页！！", function () {
                    })
                } else {
                    myAlert("已是当前页！！", function () {
                    })
                }
            } else {
                // 跳转到指定页
                window.location.href = userList_url + 'page=' + inputPage + '&searchText=' + getSearchText();
            }
        } else {
            myAlert("请输入合适的页数！！", function () {
            })
            $("#jumpNumTxt").attr("value", '')
        }
    });

    /*---------------删除操作-------------------*/

    /*   删除单个元素   */
    $('.delete').click(function () {
        var uid = this.id
        myAlertWithCancel('确定删除此项？？', function () {
            $.ajax({
                url: '/main/user_delete?uid=' + uid,
                type: 'get',
                dataType: 'json',
                success: function (data) {
                    var err_code = data.err_code
                    if (err_code === 0) {
                        myAlert('删除成功！！', function () {
                            window.location.reload()
                        })
                    } else {
                        check_err(err_code)
                    }
                }
            })
        })
    })

    /*   批量删除元素   */
    $('#deleteMore').click(function () {
        var vals = []
        var idCheck = $('input:checked[name="IDCheck"]')
        if (idCheck.length) {
            idCheck.each(function (index, element) {
                vals.push($(this).val())
            })
            myAlertWithCancel('是否选好批量删除对象？', function () {
                $.ajax({
                    url: '/main/user_delete',
                    type: 'get',
                    data: {uid: vals},
                    beforeSend: function (XMLHttpRequest) {
                        if (!idCheck.length) {
                            myAlert('没有选择删除对象！', function () {
                            })
                            XMLHttpRequest.abort()
                        }
                    },
                    dataType: 'json',
                    success: function (data) {
                        var err_code = data.err_code
                        if (err_code === 0) {
                            myAlert('删除成功！！', function () {
                                window.location.reload()
                            })
                        } else {
                            check_err(err_code)
                        }
                    }
                })
            })
        } else {
            myAlert('没有选择删除对象！', function () {
            })
        }
    })

    /*----------------搜索操作---------------------*/
    $('#searchBtn').click(function () {
        var searchText = getSearchText()
        if (searchText === '') {
            myAlert('请输入关键字！', function () {
            })
        } else {
            $.ajax({
                url: '/main/user_list',
                type: 'get',
                data: {
                    searchText: searchText,
                    page: 1,
                },
                beforeSend: function (XMLHttpRequest) {
                    if (searchText === '') {
                        myAlert('请输入关键字！', function () {
                        })
                        XMLHttpRequest.abort()
                    }
                },
                dataType: 'json',
                success: function (data) {
                    var err_code = data.err_code
                    var pages = data.pages
                    var users = data.users
                    if (err_code === 0) {
                        //  修改页数信息
                        $('#pages_total').text(pages.total)
                        $('#pages_currentPage').text(pages.currentPage)
                        $('#pages_totalPage').text(pages.totalPage)

                        $('#list_title').siblings().remove()
                        for (var i = 0; i < users.length; i++) {
                            var sex = ''
                            var check_text = ''
                            var check_style = ''
                            var checktime_text = ''
                            if (users[i].gender === 'female') {
                                sex = '女'
                            } else if (users[i].gender === 'male') {
                                sex = '男'
                            }
                            if (users[i].check_status === '1') {
                                check_text = '√'
                                check_style = 'yes_status'
                            } else {
                                check_text = '×'
                                check_style = 'no_status'
                            }
                            users[i].current_checktime == null ? checktime_text = '—' : checktime_text = users[i].current_checktime
                            var htmlText = '<tr><td><input type="checkbox"name="IDCheck"value=' + users[i].uid + '/></td><td>' + users[i].uid + '</td><td>' + users[i].name + '</td><td><span class="' + users[i].gender + '">' + sex + '</span></td><td>' + users[i].department + '</td><td><span class="' + check_style + '">' + check_text + '</span></td><td><span>' + checktime_text + '</span></td><td><a href="/main/user_more?uid=' + users[i].uid + '"class="fancybox">详情</a>&nbsp;|&nbsp;<a href="/main/user_edit?uid=' + users[i].uid + '"class="fancybox">编辑</a>&nbsp;|&nbsp;<a href="javascript:"id="' + users[i].uid + '"class="delete">删除</a></td></tr>'
                            $('#list_title').after(htmlText)
                        }
                    } else {
                        check_err(err_code)
                    }
                }
            })
        }
    })
});

function getSearchText() {
    return $('#search').val()
}
