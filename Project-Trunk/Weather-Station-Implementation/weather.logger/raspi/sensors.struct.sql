-- phpMyAdmin SQL Dump
-- version 2.11.9.6
-- http://www.phpmyadmin.net
--
-- Host: mysql
-- Generation Time: Dec 17, 2014 at 08:50 AM
-- Server version: 5.1.55
-- PHP Version: 5.3.6

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `sensors`
--

-- --------------------------------------------------------

--
-- Table structure for table `boards`
--

CREATE TABLE IF NOT EXISTS `boards` (
  `board_id` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `board_comment` varchar(128) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`board_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


-- --------------------------------------------------------

--
-- Table structure for table `sensor_data`
--

CREATE TABLE IF NOT EXISTS `sensor_data` (
  `board_id` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `sensor_id` varchar(20) COLLATE utf8_unicode_ci NOT NULL COMMENT 'like BMP180, MCP3008, etc',
  `log_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `data_type` varchar(20) COLLATE utf8_unicode_ci NOT NULL COMMENT 'TEMP, PRESSURE, SPEED, etc',
  `data_value` double NOT NULL,
  `data_comment` varchar(128) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`board_id`,`sensor_id`,`log_time`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

alter table `sensor_data` 
add constraint `data_fk_board`
foreign key (`board_id`)
references `boards`(`board_id`)
on delete cascade;

