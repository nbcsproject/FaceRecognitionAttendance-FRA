var express = require('express')
var mysql = require('mysql')

/*******************************************************/
let httpServer = require('http').Server(express)
let io = require('socket.io')(httpServer)
httpServer.listen(3001)
/*******************************************************/

var router = express.Router()


// 配置mysql连接属性
var dbConfig = require('../db/DBConfig')
var accountSQL = require('../db/account_sql')
var userSQL = require('../db/user_sql')
var checkSQL = require('../db/check_sql')

// 数据连接池
var pool = mysql.createPool(dbConfig.mysql)
if (!pool) {
    console.log('[MySql] app_remote - create Pool fail.')
} else {
    console.log('[MySql] app_remote - create Pool succeed!')
}

io.on('connection', function (socket) {

    router.get('/', function (req, res) {
        if (!req.session.account) {
            return res.redirect('/login')
        }

        return res.render('index.html', {
            account: req.session.account
        })
    })

    router.get('/app', function (req, res) {

        io.emit('event', {msg: 'ok'});
    })

    router.get('/app/login', function (req, res) {
        res.status(200).json({
            err_code: 0,
            message: 'Ok'
        })
    })

    router.post('/app/login', function (req, res) {

        var body = req.body

        pool.getConnection(function (err, connection) {

            connection.query(accountSQL.queryLogin, [body.account, body.password], function (err, result) {
                if (err) {
                    return res.status(500).end("error")
                }
                if (JSON.stringify(result) == '[]' || JSON.stringify(result) == '{}') {
                    return res.status(200).end("no")
                }
                req.session.account = result[0]
                return res.status(200).end("ok")
            })
            connection.release()
        })
    })

    router.get('/app/query', function (req, res) {

        pool.getConnection(function (err, connection) {
            connection.query(userSQL.queryAll, function (err, result) {
                if (err) {
                    return res.status(500).end("error")
                }
                usersData = JSON.parse(JSON.stringify(result))
                return res.status(200).json(usersData)
            })
            connection.release()
        })
    })

    router.post('/app/add', function (req, res) {
        var body = req.body

        userData = [
            body.uid,
            body.name,
            body.gender,
            body.phone,
            body.department,
            body.post,
            body.email,
            body.modTime
        ]
        pool.getConnection(function (err, connection) {
            connection.query(userSQL.addUser, userData, function (err, result) {
                if (err) {
                    console.log(err)
                    return res.status(500).end("error")
                }
                io.emit('event', {msg: 'ok'});
                return res.status(200).end("ok")
            })
            connection.release()
        })
    })

    router.post('/app/update', function (req, res) {
        var body = req.body

        userData = [
            body.name,
            body.gender,
            body.phone,
            body.department,
            body.post,
            body.email,
            body.modTime,
            body.uid,
        ]
        pool.getConnection(function (err, connection) {
            connection.query(userSQL.update, userData, function (err, result) {
                if (err) {
                    return res.status(500).end("error")
                }
                io.emit('event', {msg: 'ok'})
                return res.status(200).end("ok")
            })
            connection.release()
        })

    })
    router.post('/app/delete', function (req, res) {
        var body = req.body
        for (var user in body) {
            uid = body[user].uid
            pool.getConnection(function (err, connection) {
                connection.query(userSQL.delete, uid, function (err, result) {
                    if (err) {
                        return res.status(500).end("error")
                    }
                    io.emit('event', {msg: 'ok'})
                    return res.status(200).end("ok")
                })
                connection.release()
            })
        }
    })


    router.post('/app/test', function (req, res) {
        var body = req.body
        pool.getConnection(function (err, connection) {

        })
        console.log(body)
        return res.status(200).end('ok')


    })


    router.post('/app/check', function (req, res) {
        var body = req.body
        pool.getConnection(function (err, connection) {
            connection.query(checkSQL.check_in, [body.uid, body.check_time], function (err, result) {
                if (err) {
                    return res.status(500).end("error")
                }
                io.emit('event', {msg: 'ok'})
                return res.status(200).end("ok")
            })
            connection.release()
        })
    })
    router.get('/app/logout', function (req, res) {
        req.session.account = null
        return res.redirect('/login')
    })

})

module.exports = router