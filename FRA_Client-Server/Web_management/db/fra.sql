/*
Navicat MySQL Data Transfer

Source Server         : 520
Source Server Version : 50505
Source Host           : 10.10.19.134:3306
Source Database       : fra

Target Server Type    : MYSQL
Target Server Version : 50505
File Encoding         : 65001

Date: 2019-05-12 15:16:20
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for admin_user
-- ----------------------------
DROP TABLE IF EXISTS `admin_user`;
CREATE TABLE `admin_user` (
  `uid` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `account` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  PRIMARY KEY (`uid`)
) ENGINE=InnoDB AUTO_INCREMENT=1000006 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of admin_user
-- ----------------------------

-- ----------------------------
-- Table structure for check
-- ----------------------------
DROP TABLE IF EXISTS `check`;
CREATE TABLE `check` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `pid` int(11) NOT NULL,
  `uid` varchar(255) NOT NULL,
  `check_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of check
-- ----------------------------

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `pid` int(11) unsigned NOT NULL,
  `uid` varchar(255) NOT NULL COMMENT '工号',
  `name` varchar(255) NOT NULL,
  `gender` enum('female','male') NOT NULL DEFAULT 'male',
  `phone` varchar(255) NOT NULL,
  `department` varchar(255) NOT NULL,
  `post` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `check_status` enum('0','1') DEFAULT '0',
  `modTime` datetime NOT NULL,
  `current_checktime` datetime DEFAULT NULL,
  PRIMARY KEY (`uid`,`pid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of user
-- ----------------------------

-- ----------------------------
-- Table structure for ztree_business_module
-- ----------------------------
DROP TABLE IF EXISTS `ztree_business_module`;
CREATE TABLE `ztree_business_module` (
  `resourceID` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `accessPath` varchar(255) DEFAULT '',
  `parentID` int(11) NOT NULL,
  `resourceName` varchar(255) NOT NULL,
  `checked` enum('false','true') DEFAULT 'false',
  `delFlag` int(11) DEFAULT '0',
  `resourceCode` varchar(255) DEFAULT '',
  `resourceDesc` varchar(255) DEFAULT '',
  `resourceGrade` int(11) NOT NULL DEFAULT '2',
  `resourceOrder` int(11) DEFAULT '0',
  `resourceType` varchar(255) DEFAULT '',
  PRIMARY KEY (`resourceID`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ztree_business_module
-- ----------------------------
INSERT INTO `ztree_business_module` VALUES ('2', '', '1', '用户管理', 'false', '0', '', '', '2', '0', '');
INSERT INTO `ztree_business_module` VALUES ('3', '', '1', '签到管理', 'false', '0', '', '', '2', '0', '');
INSERT INTO `ztree_business_module` VALUES ('4', '/main/user_list?page=1', '2', '用户列表', 'false', '0', '', '', '2', '0', '');
INSERT INTO `ztree_business_module` VALUES ('5', '', '3', '签到详情（下一版本）', 'false', '0', '', '', '2', '0', '');
INSERT INTO `ztree_business_module` VALUES ('6', '/main/check_list?page=1', '3', '历史记录', 'false', '0', '', '', '2', '0', '');

-- ----------------------------
-- Table structure for ztree_other
-- ----------------------------
DROP TABLE IF EXISTS `ztree_other`;
CREATE TABLE `ztree_other` (
  `resourceID` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `accessPath` varchar(255) DEFAULT '',
  `parentID` int(11) NOT NULL,
  `resourceName` varchar(255) NOT NULL,
  `checked` enum('false','true') DEFAULT 'false',
  `delFlag` int(11) DEFAULT '0',
  `resourceCode` varchar(255) DEFAULT '',
  `resourceDesc` varchar(255) DEFAULT '',
  `resourceGrade` int(11) NOT NULL DEFAULT '2',
  `resourceOrder` int(11) DEFAULT '0',
  `resourceType` varchar(255) DEFAULT '',
  PRIMARY KEY (`resourceID`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ztree_other
-- ----------------------------
INSERT INTO `ztree_other` VALUES ('2', '', '1', '功能1', 'false', '0', '', '', '2', '0', '');
INSERT INTO `ztree_other` VALUES ('3', '', '1', '功能2', 'false', '0', '', '', '2', '0', '');
INSERT INTO `ztree_other` VALUES ('4', '', '2', '子功能1', 'false', '0', '', '', '2', '0', '');
INSERT INTO `ztree_other` VALUES ('5', '', '2', '子功能2', 'false', '0', '', '', '2', '0', '');
INSERT INTO `ztree_other` VALUES ('6', '', '3', '子功能1', 'false', '0', '', '', '2', '0', '');
INSERT INTO `ztree_other` VALUES ('7', '', '3', '子功能2', 'false', '0', '', '', '2', '0', '');

-- ----------------------------
-- Table structure for ztree_system_management
-- ----------------------------
DROP TABLE IF EXISTS `ztree_system_management`;
CREATE TABLE `ztree_system_management` (
  `resourceID` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `accessPath` varchar(255) DEFAULT '',
  `parentID` int(11) NOT NULL,
  `resourceName` varchar(255) NOT NULL,
  `checked` enum('false','true') DEFAULT 'false',
  `delFlag` int(11) DEFAULT '0',
  `resourceCode` varchar(255) DEFAULT '',
  `resourceDesc` varchar(255) DEFAULT '',
  `resourceGrade` int(11) NOT NULL DEFAULT '2',
  `resourceOrder` int(11) DEFAULT '0',
  `resourceType` varchar(255) DEFAULT '',
  PRIMARY KEY (`resourceID`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ztree_system_management
-- ----------------------------
INSERT INTO `ztree_system_management` VALUES ('2', '', '1', '管理员设置', 'false', '0', '', '', '2', '0', '');
INSERT INTO `ztree_system_management` VALUES ('3', '/changePassword', '2', '修改密码', 'false', '0', '', '', '2', '0', '');
INSERT INTO `ztree_system_management` VALUES ('4', '', '1', '系统设置', 'false', '0', '', '', '2', '0', '');

-- ----------------------------
-- Event structure for check_refresh
-- ----------------------------
DROP EVENT IF EXISTS `check_refresh`;
DELIMITER ;;
CREATE DEFINER=`zhangwei`@`%` EVENT `check_refresh` ON SCHEDULE EVERY 24 HOUR STARTS '2019-05-06 00:00:00' ON COMPLETION PRESERVE ENABLE DO UPDATE user SET check_status ="0"
;;
DELIMITER ;
DROP TRIGGER IF EXISTS `delete`;
DELIMITER ;;
CREATE TRIGGER `delete` AFTER DELETE ON `admin_user` FOR EACH ROW DELETE  FROM user WHERE user.pid = old.uid
;;
DELIMITER ;
DROP TRIGGER IF EXISTS `del`;
DELIMITER ;;
CREATE TRIGGER `del` AFTER DELETE ON `user` FOR EACH ROW DELETE  FROM `check` WHERE check.pid = old.pid AND  check.uid = old.uid
;;
DELIMITER ;
