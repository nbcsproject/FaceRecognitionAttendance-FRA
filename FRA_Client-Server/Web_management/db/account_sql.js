// 操作管理员账户

var AccountSQL = {
    insert: 'INSERT INTO admin_user (account,  password) VALUES (?,  ?);',
    queryAccount: 'SElECT * FROM admin_user WHERE  account= ? ;',
    queryLogin: 'SElECT * FROM admin_user WHERE  account= ? AND password=?;',
    updatePassword: 'UPDATE admin_user SET password=? WHERE account= ?',

};
module.exports = AccountSQL;