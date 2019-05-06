var express = require('express')
var mysql = require('mysql')

var md5 = require('blueimp-md5')

var router = express.Router()

// 配置mysql连接属性
var dbConfig = require('../db/DBConfig')
var accountSQL = require('../db/account_sql')

// 数据连接池
var pool = mysql.createPool(dbConfig.mysql)
if (!pool) {
    console.log('[MySql] account - create Pool fail.')
} else {
    console.log('[MySql] account - create Pool succeed!')
}

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
    // 账号密码为空验证

    if (body.account === '') {
        return res.status(500).json({
            err_code: 10,
            message: 'Account cannot be empty...'
        })
    } else if (body.password === '') {
        return res.status(500).json({
            err_code: 15,
            message: 'Password cannot be empty...'
        })
    }

    body.password = md5(md5(body.password))

    pool.getConnection(function (err, connection) {
        connection.query(accountSQL.queryLogin, [body.account, body.password], function (err, result) {
            if (err) {
                return res.status(500).json({
                    success: false,
                    message: 'Internal error...'
                })
            }
            if (JSON.stringify(result) == '[]' || JSON.stringify(result) == '{}') {
                return res.status(200).json({
                    err_code: 1,
                    message: 'Account or password is invaild...'
                })
            }
            req.session.account = result[0]
            return res.status(200).json({
                err_code: 0,
                message: 'Ok'
            })
        })
        connection.release()
    })
})


router.get('/register', function (req, res) {
    return res.render('register.html')
})

router.post('/register', function (req, res) {

    // 1. 获取表单数据
    var body = req.body

    // 账号密码为空验证
    if (body.account === '') {
        return res.status(500).json({
            err_code: 10,
            message: 'Account cannot be empty...'
        })
    } else if (body.password === '') {
        return res.status(500).json({
            err_code: 15,
            message: 'Password cannot be empty...'
        })
    }

    pool.getConnection(function (err, connection) {
        connection.query(accountSQL.queryAccount, [body.account], function (err, result) {

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
            if (!(JSON.stringify(result) == '[]' || JSON.stringify(result) == '{}')) {
                return res.status(200).json({
                    err_code: 1,
                    message: 'Email or nickname already exists..'
                })
            }
            // 对密码加密存储
            body.password = md5(md5(body.password))

            connection.query(accountSQL.insert, [body.account, body.password], function (err, result) {
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
        connection.release()
    })

})

router.get('/changePassword', function (req, res) {
    return res.render('change_password.html', {
        account: req.session.account
    })
})

router.post('/changePassword', function (req, res) {

    var body = req.body
    // 账号密码为空验证

    if ((body.old_password === '') || (body.new_password === '')) {
        return res.status(500).json({
            err_code: 15,
            message: 'Password cannot be empty...'
        })
    }

    body.old_password = md5(md5(body.old_password))
    // 验证原来密码是否正确

    pool.getConnection(function (err, connection) {
        connection.query(accountSQL.queryLogin, [body.account, body.old_password], function (err, result) {
            if (err) {
                return res.status(500).json({
                    success: false,
                    message: 'Internal error...'
                })
            }
            if (JSON.stringify(result) == '[]' || JSON.stringify(result) == '{}') {
                return res.status(200).json({
                    err_code: 1,
                    message: 'Account or password is invaild...'
                })
            }
            //修改密码
            body.new_password = md5(md5(body.new_password))
            connection.query(accountSQL.updatePassword, [body.new_password, body.account], function (err, result) {
                if (err) {
                    return res.status(500).json({
                        success: false,
                        message: 'Internal error...'
                    })
                }
            })
            // 重置登录session
            req.session.account = null
            return res.status(200).json({
                err_code: 0,
                message: 'Ok'
            })
        })
        connection.release()
    })
})

router.get('/logout', function (req, res) {
    req.session.account = null
    return res.redirect('/login')
})


module.exports = router