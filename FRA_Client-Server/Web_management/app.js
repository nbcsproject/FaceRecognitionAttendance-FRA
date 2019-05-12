var express = require('express')
var path = require('path')
var bodyParser = require('body-parser')
var session = require('express-session')

//  加载路由
var account = require('./routes/account')
var main = require('./routes/main')
var user = require('./routes/user')
var app_remote = require('./routes/app_remote')
var check = require('./routes/check')


var app = express()

app.use('/public/', express.static(path.join(__dirname, './public/')))
app.use('/node_modules/', express.static(path.join(__dirname, './node_modules/')))

app.engine('html', require('express-art-template'))
app.set('views', path.join(__dirname, './views/'))

// 配置解析表单 post请求体插件（一定要在app.use(session)前）
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
app.use(app_remote)
app.use(check)
app.use((req, res, next) => {
    res.render('404.html')
})

app.listen(3000, function () {
    console.log('Running...');
})