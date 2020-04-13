-- phpMyAdmin SQL Dump
-- version 2.11.9.6
-- http://www.phpmyadmin.net
--
-- Host: mysql
-- Generation Time: Feb 15, 2017 at 12:26 PM
-- Server version: 5.1.55
-- PHP Version: 5.3.6

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";

--
-- Database: `sensors`
--

-- --------------------------------------------------------

--
-- Table structure for table `weather_data`
--

CREATE TABLE IF NOT EXISTS `weather_data` (
  `log_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `wdir` decimal(10,3) DEFAULT NULL,
  `wgust` decimal(10,3) DEFAULT NULL,
  `wspeed` decimal(10,3) DEFAULT NULL,
  `rain` decimal(10,3) DEFAULT NULL,
  `press` decimal(10,3) DEFAULT NULL,
  `atemp` decimal(10,3) DEFAULT NULL,
  `hum` decimal(10,3) DEFAULT NULL,
  `dew` decimal(10,3) DEFAULT NULL,
  PRIMARY KEY (`log_time`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
