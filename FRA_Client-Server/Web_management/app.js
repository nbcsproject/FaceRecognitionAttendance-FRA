var express = require('express')
var path = require('path')
var bodyParser = require('body-parser')
var session = require('express-session')

//  加载路由
var account = require('./routes/account')
var main = require('./routes/main')
var user = require('./routes/user')

var app = express()

app.use('/public/', express.static(path.join(__dirname, './public/')))
app.use('/node_modules/', express.static(path.join(__dirname, './node_modules/')))

app.engine('html', require('express-art-template'))
app.set('views', path.join(__dirname, './views/'))

// 配置解析表单 post请求体插件（一定要在app.use(session)）
app.use(bodyParser.urlencoded({extended: false}))
app.use(bodyParser.json())

app.use(session({
    secret: 'keyboard cat',
    resave: false,
    saveUninitialized: true
}))

// 把路由挂到app 中
app.use(account)
app.use(main)
app.use(user)

app.listen(3000, function () {
    console.log('Running...');
})