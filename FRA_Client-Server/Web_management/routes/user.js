var express = require('express')
var mysql = require('mysql')

var md5 = require('blueimp-md5')

var router = express.Router()

// 配置mysql连接属性
var dbConfig = require('../db/DBConfig')
var userSQL = require('../db/user_sql')
var connection = mysql.createConnection(dbConfig.mysql);

//创建一个connection
connection.connect(function (err) {
    if (err) {
        console.log('[query] - :' + err);
        return;
    }
    console.log('[connection connect]  succeed!');
})


router.get('/user_list', function (req, res) {
    if (!req.session.account) {
        return res.redirect('/')
    }
    var page = {}
    page.pageSize = 10  // 每页显示10条数据
    connection.query(userSQL.total, function (err, result) {
        // 获取总记录条数
        page.total = JSON.parse(JSON.stringify(result[0])).total
        // 获取当前页,计算总页数
        page.currentPage = req.query.page
        page.totalPage = Math.ceil(page.total / page.pageSize)

        // 获取当前页面数据
        connection.query(userSQL.queryByPage, [(page.currentPage - 1) * page.pageSize, page.pageSize], function (err, result) {
            if (err) {
                return res.status(500).json({
                    success: false,
                    message: 'Internal error...'
                })
            }
            var userArray = JSON.parse(JSON.stringify(result))
            // console.log(page)  // 显示page对象
            return res.render('user_list.html', {
                users: userArray,
                pages: page
            })
        })
    })

})

router.get('/user_list/delete', function (req, res) {
    console.log(req.query._id)
    User.deleteOne({"_id": mongoose.ObjectId(req.query._id)}, function (err) {
        if (err) {
            return res.status(500).send('Server error.')
        }
        res.redirect('/user_list')
    })


})
router.get('/user_edit', function (req, res) {

    return res.render('user_edit.html', {
        user: user
    })
})

module.exports = router