var express = require('express')

var router = express.Router()


router.get('/user_list', function (req, res) {

    return res.render('user_list.html', {})
})

router.get('/loupanchart', function (req, res) {

    return res.render('loupanchart.html', {})
})

router.get('/user_edit', function (req, res) {

    return res.render('user_edit.html', {})
})

module.exports = router