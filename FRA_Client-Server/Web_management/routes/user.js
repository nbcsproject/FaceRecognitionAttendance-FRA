var express = require('express')
var mysql = require('mysql')

var router = express.Router()

// 配置mysql连接属性
var dbConfig = require('../db/DBConfig')
var userSQL = require('../db/user_sql')

// 数据连接池
var pool = mysql.createPool(dbConfig.mysql)
if (!pool) {
    console.log('[MySql] user - create Pool fail.')
} else {
    console.log('[MySql] user - create Pool succeed!')
}

router.get('/main/user_list', function (req, res) {
    if (!req.session.account) {
        return res.render('404.html')
    }

    var searchText = req.query.searchText
    var byPage = req.query.byPage

    var page = {}
    page.pageSize = 10  // 每页显示10条数据

    pool.getConnection(function (err, connection) {
        if (err) {
            return res.status(500).json({
                err_code: 3306,
                message: 'GetConnection  failed...'
            })
        }
        if (searchText || searchText === '') { //  带关键字检索
            connection.query(userSQL.search_total, [req.session.account.uid, '%' + searchText + '%'], function (err, result) {
                if (err) {
                    return res.status(500).json({
                        err_code: 3300,
                        message: 'SQL-query failed...'
                    })
                }
                // 获取总记录条数
                page.total = JSON.parse(JSON.stringify(result[0])).total
                // 获取当前页,计算总页数
                page.currentPage = req.query.page
                page.totalPage = Math.ceil(page.total / page.pageSize)
                // 获取当前页面数据
                var sql = ''
                if (page.currentPage == 1 && byPage == undefined) {
                    sql = userSQL.search_ByPage_DESC
                } else {
                    sql = userSQL.search_ByPage
                }
                connection.query(sql, [req.session.account.uid, '%' + searchText + '%', (page.currentPage - 1) * page.pageSize, page.pageSize], function (err, result) {
                    if (err) {
                        return res.status(500).json({
                            err_code: 3300,
                            message: 'SQL-query failed...'
                        })
                    }
                    var userArray = JSON.parse(JSON.stringify(result))
                    //  表示是通过page点击事件请求过来的
                    if (byPage) {
                        return res.render('user_list.html', {
                            users: userArray,
                            pages: page,
                            searchText: searchText,
                        })
                    } else if (byPage === undefined) {
                        return res.status(200).json({
                            err_code: 0,
                            users: userArray,
                            pages: page
                        })
                    }
                })
            })
            connection.release()
        } else if (searchText === undefined) {  // 正常的user_list页面显示
            connection.query(userSQL.total, [req.session.account.uid], function (err, result) {
                if (err) {
                    return res.status(500).json({
                        err_code: 3300,
                        message: 'SQL-query failed...'
                    })
                }
                // 获取总记录条数
                page.total = JSON.parse(JSON.stringify(result[0])).total
                // 获取当前页,计算总页数
                page.currentPage = req.query.page
                page.totalPage = Math.ceil(page.total / page.pageSize)
                // 获取当前页面数据

                connection.query(userSQL.queryByPage, [req.session.account.uid, (page.currentPage - 1) * page.pageSize, page.pageSize], function (err, result) {
                    if (err) {
                        return res.status(500).json({
                            err_code: 3300,
                            message: 'SQL-query failed...'
                        })
                    }
                    var userArray = JSON.parse(JSON.stringify(result))
                    return res.render('user_list.html', {
                        users: userArray,
                        pages: page,
                        searchText: '',
                    })
                })
            })
            connection.release()
        }
    })
})

router.get('/main/user_delete', function (req, res) {
    if (!req.session.account) {
        return res.render('404.html')
    }

    var uids = req.query.uid
    // 统一数据格式为  [ '100005','100006' .....]
    if (typeof uids != "object") {
        uids = [uids]
    }
    pool.getConnection(function (err, connection) {
        if (err) {
            return res.status(500).json({
                err_code: 3306,
                message: 'GetConnection  failed...'
            })
        }
        for (var i = 0; i < uids.length; i++) {

            connection.query(userSQL.delete, [uids[i], req.session.account.uid], function (err, result) {
                if (err) {
                    return res.status(500).json({
                        err_code: 3300,
                        message: 'SQL-query failed...'
                    })
                } else if (result.affectedRows > 0) {
                    return res.status(200).json({
                        err_code: 0,
                        message: 'Delete success'
                    })
                } else {
                    return res.status(500).json({
                        err_code: 20,
                        message: 'Delete failed...'
                    })
                }
            })
        }
        connection.release()
    })

})

router.get('/main/user_edit', function (req, res) {
    if (!req.session.account) {
        return res.render('404.html')
    }

    pool.getConnection(function (err, connection) {
        if (err) {
            return res.status(500).json({
                err_code: 3306,
                message: 'GetConnection  failed...'
            })
        }
        connection.query(userSQL.query, [req.session.account.uid, req.query.uid], function (err, result) {
            if (err) {
                return res.status(500).json({
                    err_code: 3300,
                    message: 'SQL-query failed...'
                })
            }
            var user = JSON.parse(JSON.stringify(result))
            return res.render('user_edit.html', {
                user: user[0]
            })
        })
        connection.release()
    })
})

router.post('/main/user_edit', function (req, res) {
    if (!req.session.account) {
        return res.render('404.html')
    }

    var body = req.body

    var timezone = 8; //目标时区时间，东八区
    var nowDate = new Date().getTime(); // 本地时间距 1970 年 1 月 1 日午夜（GMT 时间）之间的毫秒数
    var targetDate = new Date(nowDate + timezone * 60 * 60 * 1000);
    userData = [
        body.name,
        body.gender,
        body.phone,
        body.department,
        body.post,
        body.email,
        body.modTime = targetDate,
        body.uid,
        body.pid = req.session.account.uid,
    ]
    pool.getConnection(function (err, connection) {
        if (err) {
            return res.status(200).json({
                err_code: 3306,
                message: 'GetConnection  failed...'
            })
        }
        connection.query(userSQL.update, userData, function (err, result) {
            if (err) {
                return res.status(200).json({
                    err_code: 3300,
                    message: 'SQL-query failed...'
                })
            } else {
                return res.status(200).json({
                    err_code: 0,
                    message: 'Ok'
                })
            }
        })
        connection.release()
    })
})

router.get('/main/user_more', function (req, res) {
    if (!req.session.account) {
        return res.render('404.html')
    }

    pool.getConnection(function (err, connection) {
        if (err) {
            return res.status(500).json({
                err_code: 3306,
                message: 'GetConnection  failed...'
            })
        }
        connection.query(userSQL.query, [req.session.account.uid, req.query.uid], function (err, result) {
            if (err) {
                return res.status(500).json({
                    err_code: 3300,
                    message: 'SQL-query failed...'
                })
            }
            var user = JSON.parse(JSON.stringify(result))
            return res.render('user_more.html', {
                user: user[0]
            })
        })
        connection.release()
    })
})

module.exports = router
