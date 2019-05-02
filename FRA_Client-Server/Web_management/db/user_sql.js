// 操作用户信息

var UserSQL = {

    queryAll: 'SElECT * FROM user ORDER BY uid ;',
    queryByPage: 'SElECT * FROM user ORDER BY uid LIMIT ? ,? ;',
    total: 'SELECT COUNT(*) total  FROM user WHERE 1 =1 ORDER BY uid;'
};
module.exports = UserSQL;