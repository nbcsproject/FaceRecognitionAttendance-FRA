var express = require('express')
var mysql = require('mysql')

var md5 = require('blueimp-md5')

var router = express.Router()

// 配置mysql连接属性
var dbConfig = require('../db/DBConfig')
var accountSQL = require('../db/account_sql')
var connection = mysql.createConnection(dbConfig.mysql);

//创建一个connection
connection.connect(function (err) {
    if (err) {
        console.log('[query] - :' + err);
        return;
    }
    console.log('[connection connect]  succeed!');
})


router.get('/', function (req, res) {
    if (!req.session.account) {
        return res.redirect('/login')
    }

    return res.render('index.html', {
        account: req.session.account
    })
})

router.post('/', function (req, res) {

})

router.get('/login', function (req, res) {
    return res.render('login.html')
})

router.post('/login', function (req, res) {

    var body = req.body

    body.password = md5(md5(body.password))

    connection.query(accountSQL.queryLogin, [body.email, body.password], function (err, result) {
        if (err) {
            return res.status(500).json({
                success: false,
                message: 'Internal error...'
            })
        }
        if (JSON.stringify(result) == '[]' || JSON.stringify(result) == '{}') {
            return res.status(200).json({
                err_code: 1,
                message: 'Email or password is invaild...'
            })
        }
        req.session.account = result[0]
        return res.status(200).json({
            err_code: 0,
            message: 'Ok'
        })
    })
})


router.get('/register', function (req, res) {
    return res.render('register.html')
})

router.post('/register', function (req, res) {

    // 1. 获取表单数据
    var body = req.body
    connection.query(accountSQL.queryAccount, [body.email, body.nickname], function (err, result) {

        if (err) {
            return res.status(500).json({
                success: false,
                message: 'Internal error...'
            })
        }
        /* 2. 操作数据
           判断该用户是否存在
           如果已存在，不允许注册
           如果不存在，注册新用户 */
        if (!JSON.stringify(result) == '[]' || JSON.stringify(result) == '{}') {
            return res.status(200).json({
                err_code: 1,
                message: 'Email or nickname already exists..'
            })
        }
        // 对密码加密存储
        body.password = md5(md5(body.password))

        connection.query(accountSQL.insert, [body.email, body.nickname, body.password], function (err, result) {
            if (err) {
                return res.status(500).json({
                    success: false,
                    message: 'Internal error...'
                })
            }
            // 3. 发送响应
            return res.status(200).json({
                err_code: 0,
                message: 'Ok'
            })
        })
    })

})

router.get('/logout', function (req, res) {
    req.session.user = null
    return res.redirect('/login')
})


module.exports = router