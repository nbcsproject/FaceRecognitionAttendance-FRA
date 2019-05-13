var express = require('express')
var mysql = require('mysql')

var router = express.Router()

// 配置mysql连接属性
var dbConfig = require('../db/DBConfig')
var zTreeSQL = require('../db/zTree_sql')

// 数据连接池
var pool = mysql.createPool(dbConfig.mysql)
if (!pool) {
    console.log('[MySql] main - create Pool fail.')
} else {
    console.log('[MySql] main - create Pool succeed!')
}


router.get('/welcome', function (req, res) {
    return res.render('welcome.html')
})

router.get('/404', function (req, res) {
    return res.render('404.html')
})

router.get('/main/coding', function (req, res) {
    return res.render('coding.html')
})

router.get('/main/zTree', function (req, res) {
    if (!req.session.account) {
        return res.render('404.html')
    }

    var resourceType = req.query.resourceType
    var sql = ''
    if (resourceType === 'business_module') {
        sql = zTreeSQL.query_business_module
    } else if (resourceType === 'system_Management') {
        sql = zTreeSQL.query_system_Management
    } else if (resourceType === 'other') {
        sql = zTreeSQL.query_other
    }
    pool.getConnection(function (err, connection) {
        if (err) {
            return res.status(500).json({
                err_code: 3306,
                message: 'GetConnection  failed...'
            })
        }
        connection.query(sql, [resourceType], function (err, result) {
            if (err) {
                return res.status(500).json({
                    err_code: 3300,
                    message: 'SQL-query failed...'
                })
            }
            var zTreeNode = JSON.parse(JSON.stringify(result))
            return res.status(200).json(zTreeNode)
        })
        connection.release()
    })
})

module.exports = router