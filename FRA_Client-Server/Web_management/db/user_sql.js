// 操作用户信息

var UserSQL = {

    queryAll: 'SElECT * FROM user ORDER BY uid ;',
    queryByPage: 'SElECT * FROM user ORDER BY uid LIMIT ? ,? ;',
    total: 'SELECT COUNT(*) total  FROM user WHERE 1 =1 ORDER BY uid;',
    addUser: 'INSERT INTO `user` (uid,name,gender,phone,department,post,email,modTime) VALUES(?,?,?,?,?,?,?,?)',
    update: 'UPDATE fra.user SET name=?,gender=?,phone=?,department=?,post=?,email=?,modTime=? WHERE uid=?',
    delete: 'DELETE FROM user WHERE uid = ?',
};
module.exports = UserSQL;