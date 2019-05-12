var express = require('express')
var mysql = require('mysql')

var router = express.Router()

// 配置mysql连接属性
var dbConfig = require('../db/DBConfig')
var checkSQL = require('../db/check_sql')

// 数据连接池
var pool = mysql.createPool(dbConfig.mysql)
if (!pool) {
    console.log('[MySql] check - create Pool fail.')
} else {
    console.log('[MySql] check - create Pool succeed!')
}

router.get('/main/check_list', function (req, res) {
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
            connection.query(checkSQL.search_list_total, ['%' + searchText + '%', req.session.account.uid], function (err, result) {
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
                    sql = checkSQL.search_list_ByPage_DESC
                } else {
                    sql = checkSQL.search_list_ByPage
                }
                connection.query(sql, ['%' + searchText + '%', req.session.account.uid, page.pageSize, (page.currentPage - 1) * page.pageSize], function (err, result) {
                    if (err) {
                        return res.status(500).json({
                            err_code: 3300,
                            message: 'SQL-query failed...'
                        })
                    }
                    var userArray = JSON.parse(JSON.stringify(result))
                    //  表示是通过page点击事件请求过来的
                    if (byPage) {
                        return res.render('check_list.html', {
                            users: userArray,
                            pages: page,
                            searchText: searchText,
                        })
                    }
                    if (byPage === undefined) {
                        return res.status(200).json({
                            err_code: 0,
                            users: userArray,
                            pages: page
                        })
                    }
                })
            })
            connection.release()
        } else if (searchText === undefined) {  // 正常的check_list页面显示
            connection.query(checkSQL.check_query_total, [req.session.account.uid], function (err, result) {
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
                connection.query(checkSQL.check_query_ByPage, [req.session.account.uid, page.pageSize, (page.currentPage - 1) * page.pageSize], function (err, result) {
                    if (err) {
                        return res.status(500).json({
                            err_code: 3300,
                            message: 'SQL-query failed...'
                        })
                    }
                    var userArray = JSON.parse(JSON.stringify(result))
                    return res.render('check_list.html', {
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

router.get('/main/check_delete', function (req, res) {
    if (!req.session.account) {
        return res.render('404.html')
    }

    var ids = req.query.id
    // 统一数据格式为  [ '100005','100006' .....]
    if (typeof ids != "object") {
        ids = [ids]
    }
    pool.getConnection(function (err, connection) {
        if (err) {
            return res.status(500).json({
                err_code: 3306,
                message: 'GetConnection  failed...'
            })
        }
        for (var i = 0; i < ids.length; i++) {
            connection.query(checkSQL.check_delete, [parseInt(ids[i])], function (err, result) {
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

module.exports = router
