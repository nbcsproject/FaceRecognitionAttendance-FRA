var mongoose = require('mongoose')

//连接数据库
mongoose.connect('mongodb://127.0.0.1:27017/test', {useNewUrlParser: true})

mongoose.connection.on("error", function (error) {
    console.log("数据库连接失败：" + error);
});
mongoose.connection.on("open", function () {
    console.log("------数据库连接成功（user）！------");
});

var Schema = mongoose.Schema

var userSchema = new Schema({
    email: {
        type: String,
        required: true
    },
    uid: {
        type: Number,
        required: true
    },
    username: {
        type: String,
        required: true
    },
    gender: {
        type: Number,
        enum: [0, 1],
        default: 0
    },
    group: {
        type: String,
        required: true
    },
    check_status: {
        type: Boolean,
        default: false
    },
    check_time: {
        type: Date,
        default: Date.now
    },
    create_time: {
        type: Date,
        default: Date.now
    },
    last_modified_time: {
        type: Date,
        default: Date.now
    },
    avatar: {//头像
        type: String,
        default: '/public/img/avatar.png'
    },
})

module.exports = mongoose.model('User', userSchema)