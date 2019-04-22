var express = require('express')
var User = require('../models/account')
var md5 = require('blueimp-md5')

var router = express.Router()


router.get('/', function (req, res) {
    if (!req.session.user) {
        return res.redirect('/login')
    }

    return res.render('index.html', {
        user: req.session.user
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

    User.findOne({
        email: body.email,
        password: body.password
    }, function (err, user) {
        if (err) {
            return res.status(500).json({
                err_code: 500,
                message: err.message
            })
        }

        if (!user) {
            return res.status(200).json({
                err_code: 1,
                message: 'Email or password is invaild...'
            })
        }

        req.session.user = user
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

    var body = req.body

    // 1. 获取表单数据
    User.findOne({
        $or: [
            {
                email: body.email
            },
            {
                nickname: body.nickname
            }
        ]
    }, function (err, data) {
        if (err) {
            return res.status(500).json({
                success: false,
                message: 'Internal error...'
            })
        }

        /*
        2. 操作数据
           判断该用户是否存在
           如果已存在，不允许注册
           如果不存在，注册新用户
         */
        if (data) {
            return res.status(200).json({
                err_code: 1,
                message: 'Email or nickname already exists..'
            })
        }

        body.password = md5(md5(body.password))

        new User(body).save(function (err, user) {
            if (err) {
                return res.status(500).json({
                    err_code: 500,
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