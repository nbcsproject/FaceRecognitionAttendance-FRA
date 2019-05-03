// 操作管理员账户

var UserSQL = {
    insert: 'INSERT INTO account (email, nickname, password) VALUES (?, ?, ?);',
    queryAccount: 'SElECT * FROM account WHERE  email= ? OR nickname=?;',
    queryLogin: 'SElECT * FROM account WHERE  email= ? AND password=?;',

};
module.exports = UserSQL;