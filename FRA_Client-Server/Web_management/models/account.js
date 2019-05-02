var mongoose = require('mongoose')

//连接数据库
mongoose.connect('mongodb://127.0.0.1:27017/test', {useNewUrlParser: true})

mongoose.connection.on("error", function (error) {
	console.log("数据库连接失败：" + error);
});
mongoose.connection.on("open", function () {
	console.log("------数据库连接成功(account)！------");
});

var Schema = mongoose.Schema

var accountSchema = new Schema({
	email: {
		type: String,
		required: true
	},
	nickname: {
		type: String,
		required: true
	},
	password: {
		type: String,
		required: true
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
	bio: {//介绍
		type: String,
		default: ''
	},
	gender: {
		type: Number,
		enum: [-1, 0, 1],
		default: -1
	},
	birthday: {
		default: Date
	},
	status: {
		type: Number,
		// 0 没有任何权限限制
		enum: [0, 1, 2],
		default: 0
	}

})

module.exports = mongoose.model('Account', accountSchema)