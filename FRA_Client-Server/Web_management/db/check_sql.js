var checkSQL = {
    check_in: 'INSERT INTO `check` (uid,check_time,pid) VALUES(?,?,?)',
    check_flag: 'UPDATE user SET check_status ="1",current_checktime =? WHERE uid=? AND pid=?',

    // 无关键字查询

    check_query_ByPage: 'SELECT a.*,b.id , b.check_time FROM (SELECT * FROM `user` WHERE  pid =?) AS a JOIN `check` b ON a.uid = b.uid AND a.pid = b.pid ORDER BY uid , check_time DESC LIMIT ? OFFSET ?;',
    check_query_total: 'SELECT COUNT(*) total FROM (SELECT * FROM `user` WHERE  pid =?) AS a JOIN `check` b ON a.uid = b.uid AND a.pid = b.pid ;',

    check_delete: 'DELETE FROM `check` WHERE id = ? ',
    // 关键字查询
    search_list_ByPage: 'SELECT a.*, b.id , b.check_time FROM (SELECT * FROM `user` WHERE `name` LIKE ? AND pid =?) AS a JOIN `check` b ON a.uid = b.uid AND a.pid = b.pid ORDER BY uid , check_time DESC LIMIT ? OFFSET ?;',
    search_list_ByPage_DESC: 'SELECT a.*, b.id ,b.check_time FROM (SELECT * FROM `user` WHERE `name` LIKE ? AND pid =?) AS a JOIN `check` b ON a.uid = b.uid AND a.pid = b.pid ORDER BY uid , check_time LIMIT ? OFFSET ?;',
    search_list_total: 'SELECT COUNT(*) total FROM (SELECT * FROM `user` WHERE `name` LIKE ? AND pid =?) AS a JOIN `check` b ON a.uid = b.uid AND a.pid = b.pid ;',

};
module.exports = checkSQL;