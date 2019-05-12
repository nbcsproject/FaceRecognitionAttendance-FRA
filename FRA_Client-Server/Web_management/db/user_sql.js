// 操作用户信息

var UserSQL = {
    query: 'SElECT * FROM user WHERE pid =? AND uid=?',
    queryAll: 'SElECT * FROM user WHERE pid =? ORDER BY uid  ;',

    // 无关键字查询
    queryByPage: 'SElECT * FROM user WHERE pid =?  ORDER BY uid LIMIT ? ,?  ;',
    total: 'SELECT COUNT(*) total  FROM user WHERE  pid =?  ;',

    addUser: 'INSERT INTO `user` (uid,name,gender,phone,department,post,email,modTime,pid) VALUES(?,?,?,?,?,?,?,?,?)',
    update: 'UPDATE user SET name=?,gender=?,phone=?,department=?,post=?,email=?,modTime=? WHERE uid=? AND pid=?',
    delete: 'DELETE FROM user WHERE uid = ? AND pid=?',

    // 有关键字查询
    search_ByPage_DESC: 'SElECT * FROM (SElECT * FROM  user WHERE pid =? AND `name` like ?  LIMIT ? ,? ) as a ORDER BY uid DESC;',
    search_ByPage: 'SElECT * FROM  user WHERE pid =? AND `name` like ?  LIMIT ? ,? ;',
    search_total: 'SELECT COUNT(*) total  FROM user WHERE  pid =? AND `name` like ? ORDER BY uid ;',
};
module.exports = UserSQL;