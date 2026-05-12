CREATE DATABASE  IF NOT EXISTS `LTUDM` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `LTUDM`;
-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: LTUDM
-- ------------------------------------------------------
-- Server version	8.4.8

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `attachments`
--

DROP TABLE IF EXISTS `attachments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `attachments` (
  `id` char(36) NOT NULL,
  `message_id` char(36) NOT NULL,
  `file_url` varchar(700) DEFAULT NULL,
  `file_name` varchar(255) DEFAULT NULL,
  `mime_type` varchar(120) DEFAULT NULL,
  `file_size` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `message_id` (`message_id`),
  CONSTRAINT `attachments_ibfk_1` FOREIGN KEY (`message_id`) REFERENCES `messages` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `attachments`
--

LOCK TABLES `attachments` WRITE;
/*!40000 ALTER TABLE `attachments` DISABLE KEYS */;
/*!40000 ALTER TABLE `attachments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `blocks`
--

DROP TABLE IF EXISTS `blocks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `blocks` (
  `blocker_id` char(36) NOT NULL,
  `blocked_id` char(36) NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`blocker_id`,`blocked_id`),
  KEY `blocked_id` (`blocked_id`),
  CONSTRAINT `blocks_ibfk_1` FOREIGN KEY (`blocker_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `blocks_ibfk_2` FOREIGN KEY (`blocked_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_no_self_block` CHECK ((`blocker_id` <> `blocked_id`))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `blocks`
--

LOCK TABLES `blocks` WRITE;
/*!40000 ALTER TABLE `blocks` DISABLE KEYS */;
/*!40000 ALTER TABLE `blocks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conversation_members`
--

DROP TABLE IF EXISTS `conversation_members`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conversation_members` (
  `conversation_id` char(36) NOT NULL,
  `user_id` char(36) NOT NULL,
  `role` enum('OWNER','ADMIN','MEMBER') DEFAULT 'MEMBER',
  `joined_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`conversation_id`,`user_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `conversation_members_ibfk_1` FOREIGN KEY (`conversation_id`) REFERENCES `conversations` (`id`) ON DELETE CASCADE,
  CONSTRAINT `conversation_members_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conversation_members`
--

LOCK TABLES `conversation_members` WRITE;
/*!40000 ALTER TABLE `conversation_members` DISABLE KEYS */;
INSERT INTO `conversation_members` VALUES ('1853096a-b11b-4ecb-9a03-e74cc5c22f18','54d52ba1-a244-42c9-8af7-f5154f008c1b','OWNER','2026-05-07 11:48:06'),('1853096a-b11b-4ecb-9a03-e74cc5c22f18','da4846a4-a26f-42ec-824b-009f7221e3b5','MEMBER','2026-05-07 11:48:06'),('2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','54d52ba1-a244-42c9-8af7-f5154f008c1b','OWNER','2026-05-07 09:46:39'),('2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','MEMBER','2026-05-07 09:46:39'),('31055b4f-ebc4-4a95-9a51-114e2d580d56','07a28f6d-2f10-4813-bb49-1fcf927b01fe','MEMBER','2026-05-07 15:40:50'),('31055b4f-ebc4-4a95-9a51-114e2d580d56','54d52ba1-a244-42c9-8af7-f5154f008c1b','OWNER','2026-05-07 15:40:50'),('3588ebc0-5f49-4112-b793-3975d091268d','61b01bab-16a8-4979-8a83-c8e871561da2','MEMBER','2026-04-18 03:40:10'),('3588ebc0-5f49-4112-b793-3975d091268d','b6d37a68-80fa-4706-8cc7-b7983413651b','OWNER','2026-04-18 03:40:10'),('881b86ba-9d28-446a-a2fd-bfd454b5d68f','27c24d81-0c5d-44f9-9443-44199a13904a','OWNER','2026-05-07 15:56:50'),('881b86ba-9d28-446a-a2fd-bfd454b5d68f','7e9285fd-dbe4-4eaf-809d-d0043557a216','MEMBER','2026-05-07 15:56:50');
/*!40000 ALTER TABLE `conversation_members` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conversations`
--

DROP TABLE IF EXISTS `conversations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conversations` (
  `id` char(36) NOT NULL,
  `type` enum('DIRECT','GROUP') NOT NULL,
  `title` varchar(150) DEFAULT NULL,
  `created_by` char(36) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `avatar_url` varchar(700) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `created_by` (`created_by`),
  CONSTRAINT `conversations_ibfk_1` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conversations`
--

LOCK TABLES `conversations` WRITE;
/*!40000 ALTER TABLE `conversations` DISABLE KEYS */;
INSERT INTO `conversations` VALUES ('1853096a-b11b-4ecb-9a03-e74cc5c22f18','DIRECT',NULL,'54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 11:48:06',NULL),('2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','DIRECT',NULL,'54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 09:46:39',NULL),('31055b4f-ebc4-4a95-9a51-114e2d580d56','DIRECT',NULL,'54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 15:40:50',NULL),('3588ebc0-5f49-4112-b793-3975d091268d','DIRECT',NULL,'b6d37a68-80fa-4706-8cc7-b7983413651b','2026-04-18 03:40:10',NULL),('881b86ba-9d28-446a-a2fd-bfd454b5d68f','DIRECT',NULL,'27c24d81-0c5d-44f9-9443-44199a13904a','2026-05-07 15:56:50',NULL);
/*!40000 ALTER TABLE `conversations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `friendships`
--

DROP TABLE IF EXISTS `friendships`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `friendships` (
  `id` char(36) NOT NULL,
  `requester_id` char(36) NOT NULL,
  `addressee_id` char(36) NOT NULL,
  `status` enum('PENDING','ACCEPTED','DECLINED','BLOCKED') DEFAULT 'PENDING',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `requester_id` (`requester_id`,`addressee_id`),
  KEY `addressee_id` (`addressee_id`),
  CONSTRAINT `friendships_ibfk_1` FOREIGN KEY (`requester_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `friendships_ibfk_2` FOREIGN KEY (`addressee_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_no_self_friendship` CHECK ((`requester_id` <> `addressee_id`))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `friendships`
--

LOCK TABLES `friendships` WRITE;
/*!40000 ALTER TABLE `friendships` DISABLE KEYS */;
/*!40000 ALTER TABLE `friendships` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `message_deletions`
--

DROP TABLE IF EXISTS `message_deletions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `message_deletions` (
  `message_id` char(36) NOT NULL,
  `user_id` char(36) NOT NULL,
  `deleted_at` datetime DEFAULT NULL,
  PRIMARY KEY (`message_id`,`user_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `message_deletions_ibfk_1` FOREIGN KEY (`message_id`) REFERENCES `messages` (`id`) ON DELETE CASCADE,
  CONSTRAINT `message_deletions_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `message_deletions`
--

LOCK TABLES `message_deletions` WRITE;
/*!40000 ALTER TABLE `message_deletions` DISABLE KEYS */;
/*!40000 ALTER TABLE `message_deletions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `message_receipts`
--

DROP TABLE IF EXISTS `message_receipts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `message_receipts` (
  `message_id` char(36) NOT NULL,
  `user_id` char(36) NOT NULL,
  `seen_at` datetime DEFAULT NULL,
  PRIMARY KEY (`message_id`,`user_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `message_receipts_ibfk_1` FOREIGN KEY (`message_id`) REFERENCES `messages` (`id`) ON DELETE CASCADE,
  CONSTRAINT `message_receipts_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `message_receipts`
--

LOCK TABLES `message_receipts` WRITE;
/*!40000 ALTER TABLE `message_receipts` DISABLE KEYS */;
INSERT INTO `message_receipts` VALUES ('01303777-ce01-403c-963a-c23dccb1aee9','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 10:44:39'),('01303777-ce01-403c-963a-c23dccb1aee9','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('013cfb99-00fa-4863-b05a-e8b6c5411d4e','7e9285fd-dbe4-4eaf-809d-d0043557a216','2026-05-07 15:57:28'),('036152a1-1b96-4d05-b5da-c4982f7b789f','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 10:48:56'),('036152a1-1b96-4d05-b5da-c4982f7b789f','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('041b3466-9b04-41ff-8b65-1be9ae1b3205','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 10:48:56'),('041b3466-9b04-41ff-8b65-1be9ae1b3205','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('043fe6b2-ee06-4350-b828-e5efc39db9a0','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 10:48:56'),('043fe6b2-ee06-4350-b828-e5efc39db9a0','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('0be59e24-ef12-4d87-9b86-99d43b113ad7','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 10:48:56'),('0be59e24-ef12-4d87-9b86-99d43b113ad7','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('0d44e39e-c2a7-4387-aae7-70228b71500a','27c24d81-0c5d-44f9-9443-44199a13904a','2026-05-07 15:59:57'),('0d79e5d2-7589-4323-b2b4-2fbc6a0f91ab','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 10:48:56'),('0d79e5d2-7589-4323-b2b4-2fbc6a0f91ab','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('0f7247c6-d0c6-4be4-892a-96a80298b877','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 10:48:56'),('0f7247c6-d0c6-4be4-892a-96a80298b877','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('148681ba-2f7f-45f0-a55f-3f44c7b499d7','6e13b1b5-7ddd-45e7-b0e2-83514958895f','2026-05-07 10:47:00'),('148681ba-2f7f-45f0-a55f-3f44c7b499d7','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('16fd0fae-03de-4fe8-ab6f-f500d8ac35f0','27c24d81-0c5d-44f9-9443-44199a13904a','2026-05-07 15:58:11'),('16fd0fae-03de-4fe8-ab6f-f500d8ac35f0','7e9285fd-dbe4-4eaf-809d-d0043557a216','2026-05-07 15:57:28'),('1f98560d-92ef-4189-8f21-b2aeda5680f9','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 10:48:56'),('1f98560d-92ef-4189-8f21-b2aeda5680f9','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('228816b8-a3b9-4fa7-8918-8ff82972c1c6','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 10:44:39'),('228816b8-a3b9-4fa7-8918-8ff82972c1c6','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('2498cc07-4b74-400b-9405-c1f77be6648f','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 10:48:56'),('2498cc07-4b74-400b-9405-c1f77be6648f','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('254f3d71-cd1a-4bff-923c-3abd65c3340f','07a28f6d-2f10-4813-bb49-1fcf927b01fe','2026-05-07 15:51:57'),('26722c09-9d40-4317-ba20-b5bfccf76915','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 10:48:56'),('26722c09-9d40-4317-ba20-b5bfccf76915','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('330d8385-19a4-4b50-a72d-bea46a6709aa','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 10:44:39'),('330d8385-19a4-4b50-a72d-bea46a6709aa','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('39473d69-6b69-4bf7-81a7-4496da15eaa2','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 14:18:22'),('4489e1fa-0150-46b9-b711-284709c32046','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 10:48:56'),('4489e1fa-0150-46b9-b711-284709c32046','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('46c473e8-a4f3-416c-b572-46a1b5634a66','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 10:48:56'),('46c473e8-a4f3-416c-b572-46a1b5634a66','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('5789e699-b823-4e46-8751-bb83db701e4f','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 10:48:56'),('5789e699-b823-4e46-8751-bb83db701e4f','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('58dadb05-3b2e-400a-a214-4d105dc19e01','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 10:48:56'),('58dadb05-3b2e-400a-a214-4d105dc19e01','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('68132455-b318-456b-ab9a-f96f81f25164','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 10:48:56'),('68132455-b318-456b-ab9a-f96f81f25164','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('69d85b45-755e-439e-bccf-3024db3e9629','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 10:48:56'),('69d85b45-755e-439e-bccf-3024db3e9629','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('6d99e4f8-37e3-4a90-9740-774b3a3be5ef','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 10:48:56'),('6d99e4f8-37e3-4a90-9740-774b3a3be5ef','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('79a73a60-aec6-4fcd-8fe3-1a545a2517ae','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 10:48:56'),('79a73a60-aec6-4fcd-8fe3-1a545a2517ae','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('7a5ce472-e299-42b2-a65b-f4b66099181b','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 15:41:04'),('7cdf959b-35cc-4fc4-85bd-091c057e8818','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 10:48:56'),('7cdf959b-35cc-4fc4-85bd-091c057e8818','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('84e62eef-acb9-48b3-92a7-7ddd99fec6f0','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 10:48:56'),('84e62eef-acb9-48b3-92a7-7ddd99fec6f0','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('861c784f-3623-47ee-9e4d-adbc4b47171f','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 10:48:56'),('861c784f-3623-47ee-9e4d-adbc4b47171f','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('89c543bc-0dee-4529-9061-db05467096c6','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 14:14:16'),('901bfe15-a2d3-4d36-801e-822da608e115','6e13b1b5-7ddd-45e7-b0e2-83514958895f','2026-05-07 11:00:07'),('901bfe15-a2d3-4d36-801e-822da608e115','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('9fa88248-a0c1-4442-8857-ec584fe74c9a','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 10:48:56'),('9fa88248-a0c1-4442-8857-ec584fe74c9a','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('a592bfea-fcf2-4d14-9ab3-4eb40d84d0ca','6e13b1b5-7ddd-45e7-b0e2-83514958895f','2026-05-07 10:47:00'),('a592bfea-fcf2-4d14-9ab3-4eb40d84d0ca','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('a7e952bc-bec3-4ecd-8d33-e9cf17418d08','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 10:48:56'),('a7e952bc-bec3-4ecd-8d33-e9cf17418d08','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('a7fa9f56-7309-4ab7-83ba-b233b3c3943e','6e13b1b5-7ddd-45e7-b0e2-83514958895f','2026-05-07 10:47:00'),('a7fa9f56-7309-4ab7-83ba-b233b3c3943e','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('acf82828-3e4b-4c0c-9788-6e7f96278503','6e13b1b5-7ddd-45e7-b0e2-83514958895f','2026-05-07 10:47:00'),('acf82828-3e4b-4c0c-9788-6e7f96278503','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('b5ef1178-1962-468b-9c23-a49d7bcad439','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 10:48:56'),('b5ef1178-1962-468b-9c23-a49d7bcad439','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('b7fbddba-fd06-4b4c-9878-5386175683f4','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 10:48:56'),('b7fbddba-fd06-4b4c-9878-5386175683f4','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('bd5f787e-a4d0-4474-b07d-1174ccb38635','6e13b1b5-7ddd-45e7-b0e2-83514958895f','2026-05-07 10:47:00'),('bd5f787e-a4d0-4474-b07d-1174ccb38635','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('be8ee4ec-62a2-4cd0-9423-d1afea2803e5','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 10:48:56'),('be8ee4ec-62a2-4cd0-9423-d1afea2803e5','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('c00df335-7558-42db-8cb5-9a220c620ede','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-12 10:12:36'),('c8928e30-ed98-48c4-8f9f-e82d8732edd5','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 10:48:56'),('c8928e30-ed98-48c4-8f9f-e82d8732edd5','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('cc236b8a-a77f-4c95-bd6b-d5dc20a1756a','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 10:48:56'),('cc236b8a-a77f-4c95-bd6b-d5dc20a1756a','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('cf7f35ae-ba17-465e-895d-cb9976959590','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 10:48:56'),('cf7f35ae-ba17-465e-895d-cb9976959590','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('cfe07159-3c66-4dfa-adb6-7017c9647183','7e9285fd-dbe4-4eaf-809d-d0043557a216','2026-05-07 15:57:28'),('d1fedb75-35d2-430b-83e9-675fcdbefa91','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 10:48:56'),('d1fedb75-35d2-430b-83e9-675fcdbefa91','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('d2a61373-bf0f-4669-b787-0b95d44824b3','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 14:18:22'),('d3862e59-6467-44cd-bcf2-207159df6a46','6e13b1b5-7ddd-45e7-b0e2-83514958895f','2026-05-07 10:47:00'),('d3862e59-6467-44cd-bcf2-207159df6a46','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('db488197-4b8c-4d2c-a874-f31b37238843','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 10:48:56'),('db488197-4b8c-4d2c-a874-f31b37238843','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('dccd1596-927d-49f6-bc62-bbca5001706e','27c24d81-0c5d-44f9-9443-44199a13904a','2026-05-07 15:58:11'),('ea2c127b-d604-4205-9cdb-5c3d9ae0df58','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 14:15:59'),('ebe5a9cc-8afb-4165-a37a-7dfcfa6b5da4','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 10:48:56'),('ebe5a9cc-8afb-4165-a37a-7dfcfa6b5da4','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('f05dbb35-7c82-4066-9726-2ed597371d61','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 10:48:56'),('f05dbb35-7c82-4066-9726-2ed597371d61','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('f2c16e92-37bf-4ffb-bda0-900aecc2da88','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 10:48:56'),('f2c16e92-37bf-4ffb-bda0-900aecc2da88','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('f40be617-0fb4-49df-8470-34f7ef5f0a4d','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 14:14:16'),('faa274f3-faae-4f84-82f2-ebb5ce668f03','6e13b1b5-7ddd-45e7-b0e2-83514958895f','2026-05-07 12:04:51'),('fad2cd92-be84-4b22-a672-3c0fd703a6b7','6e13b1b5-7ddd-45e7-b0e2-83514958895f','2026-05-07 10:47:48'),('fad2cd92-be84-4b22-a672-3c0fd703a6b7','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('fbd1d87b-1fa7-496a-88d8-047999629be2','6e13b1b5-7ddd-45e7-b0e2-83514958895f','2026-05-07 10:47:00'),('fbd1d87b-1fa7-496a-88d8-047999629be2','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('fd14d12f-4a05-4405-b8e3-87e4b896efb1','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 10:48:56'),('fd14d12f-4a05-4405-b8e3-87e4b896efb1','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00'),('fd7c1f42-030a-4957-a8d4-44cf4ec2005f','54d52ba1-a244-42c9-8af7-f5154f008c1b','2026-05-07 10:48:56'),('fd7c1f42-030a-4957-a8d4-44cf4ec2005f','da4846a4-a26f-42ec-824b-009f7221e3b5','2026-05-07 11:12:00');
/*!40000 ALTER TABLE `message_receipts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `messages`
--

DROP TABLE IF EXISTS `messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `messages` (
  `id` char(36) NOT NULL,
  `conversation_id` char(36) NOT NULL,
  `sender_id` char(36) NOT NULL,
  `type` enum('TEXT','FILE','IMAGE','SYSTEM') DEFAULT 'TEXT',
  `content` text,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_edited` tinyint(1) DEFAULT '0',
  `edited_at` datetime DEFAULT NULL,
  `is_recalled` tinyint(1) DEFAULT '0',
  `recalled_at` datetime DEFAULT NULL,
  `recalled_by` char(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `conversation_id` (`conversation_id`),
  KEY `sender_id` (`sender_id`),
  KEY `recalled_by` (`recalled_by`),
  CONSTRAINT `messages_ibfk_1` FOREIGN KEY (`conversation_id`) REFERENCES `conversations` (`id`) ON DELETE CASCADE,
  CONSTRAINT `messages_ibfk_2` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`),
  CONSTRAINT `messages_ibfk_3` FOREIGN KEY (`recalled_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `messages`
--

LOCK TABLES `messages` WRITE;
/*!40000 ALTER TABLE `messages` DISABLE KEYS */;
INSERT INTO `messages` VALUES ('01303777-ce01-403c-963a-c23dccb1aee9','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','TEXT','abc','2026-05-07 10:23:09',0,NULL,0,NULL,NULL),('013cfb99-00fa-4863-b05a-e8b6c5411d4e','881b86ba-9d28-446a-a2fd-bfd454b5d68f','27c24d81-0c5d-44f9-9443-44199a13904a','TEXT','sss','2026-05-07 15:57:18',0,NULL,0,NULL,NULL),('036152a1-1b96-4d05-b5da-c4982f7b789f','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','TEXT','a','2026-05-07 10:48:03',0,NULL,0,NULL,NULL),('041b3466-9b04-41ff-8b65-1be9ae1b3205','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','TEXT','a','2026-05-07 10:47:56',0,NULL,0,NULL,NULL),('043fe6b2-ee06-4350-b828-e5efc39db9a0','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','TEXT','a','2026-05-07 10:47:54',0,NULL,0,NULL,NULL),('0be59e24-ef12-4d87-9b86-99d43b113ad7','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','TEXT','a','2026-05-07 10:47:55',0,NULL,0,NULL,NULL),('0c971a8b-2784-4f84-babc-d608c296ba22','1853096a-b11b-4ecb-9a03-e74cc5c22f18','54d52ba1-a244-42c9-8af7-f5154f008c1b','TEXT','xdxd','2026-05-07 14:16:38',0,NULL,0,NULL,NULL),('0d44e39e-c2a7-4387-aae7-70228b71500a','881b86ba-9d28-446a-a2fd-bfd454b5d68f','7e9285fd-dbe4-4eaf-809d-d0043557a216','TEXT','nnn','2026-05-07 15:59:10',0,NULL,0,NULL,NULL),('0d79e5d2-7589-4323-b2b4-2fbc6a0f91ab','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','TEXT','a','2026-05-07 10:48:03',0,NULL,0,NULL,NULL),('0f7247c6-d0c6-4be4-892a-96a80298b877','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','TEXT','a','2026-05-07 10:47:57',0,NULL,0,NULL,NULL),('148681ba-2f7f-45f0-a55f-3f44c7b499d7','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','54d52ba1-a244-42c9-8af7-f5154f008c1b','TEXT','chào','2026-05-07 10:23:36',0,NULL,0,NULL,NULL),('16fd0fae-03de-4fe8-ab6f-f500d8ac35f0','881b86ba-9d28-446a-a2fd-bfd454b5d68f','07a28f6d-2f10-4813-bb49-1fcf927b01fe','TEXT','alo','2026-05-07 15:57:02',0,NULL,0,NULL,NULL),('1f98560d-92ef-4189-8f21-b2aeda5680f9','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','TEXT','a','2026-05-07 10:47:55',0,NULL,0,NULL,NULL),('20a64218-a00b-4753-bcfb-9d5587a2a810','1853096a-b11b-4ecb-9a03-e74cc5c22f18','54d52ba1-a244-42c9-8af7-f5154f008c1b','TEXT','lô','2026-05-07 14:16:08',0,NULL,0,NULL,NULL),('228816b8-a3b9-4fa7-8918-8ff82972c1c6','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','TEXT','chào cậu','2026-05-07 10:31:45',0,NULL,0,NULL,NULL),('2498cc07-4b74-400b-9405-c1f77be6648f','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','TEXT','a','2026-05-07 10:47:54',0,NULL,0,NULL,NULL),('254f3d71-cd1a-4bff-923c-3abd65c3340f','31055b4f-ebc4-4a95-9a51-114e2d580d56','54d52ba1-a244-42c9-8af7-f5154f008c1b','TEXT','Giề','2026-05-07 15:41:22',0,NULL,0,NULL,NULL),('26722c09-9d40-4317-ba20-b5bfccf76915','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','TEXT','Ok ổn','2026-05-07 10:47:22',0,NULL,0,NULL,NULL),('330d8385-19a4-4b50-a72d-bea46a6709aa','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','TEXT','abc','2026-05-07 10:31:37',0,NULL,0,NULL,NULL),('33bc9212-e9be-472f-92e6-91a20cc36a52','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','54d52ba1-a244-42c9-8af7-f5154f008c1b','TEXT','abcde','2026-05-07 14:09:11',0,NULL,0,NULL,NULL),('39473d69-6b69-4bf7-81a7-4496da15eaa2','1853096a-b11b-4ecb-9a03-e74cc5c22f18','da4846a4-a26f-42ec-824b-009f7221e3b5','TEXT','Gay','2026-05-07 14:17:14',0,NULL,0,NULL,NULL),('4489e1fa-0150-46b9-b711-284709c32046','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','TEXT','a','2026-05-07 10:47:58',0,NULL,0,NULL,NULL),('46c473e8-a4f3-416c-b572-46a1b5634a66','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','TEXT','a','2026-05-07 10:48:02',0,NULL,0,NULL,NULL),('5789e699-b823-4e46-8751-bb83db701e4f','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','TEXT','a','2026-05-07 10:47:57',0,NULL,0,NULL,NULL),('58dadb05-3b2e-400a-a214-4d105dc19e01','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','TEXT','a','2026-05-07 10:48:02',0,NULL,0,NULL,NULL),('68132455-b318-456b-ab9a-f96f81f25164','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','TEXT','â','2026-05-07 10:48:01',0,NULL,0,NULL,NULL),('69d85b45-755e-439e-bccf-3024db3e9629','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','TEXT','a','2026-05-07 10:47:57',0,NULL,0,NULL,NULL),('6d99e4f8-37e3-4a90-9740-774b3a3be5ef','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','TEXT','a','2026-05-07 10:48:01',0,NULL,0,NULL,NULL),('79a73a60-aec6-4fcd-8fe3-1a545a2517ae','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','TEXT','a','2026-05-07 10:48:03',0,NULL,0,NULL,NULL),('7a5ce472-e299-42b2-a65b-f4b66099181b','31055b4f-ebc4-4a95-9a51-114e2d580d56','07a28f6d-2f10-4813-bb49-1fcf927b01fe','TEXT','alo','2026-05-07 15:41:03',0,NULL,0,NULL,NULL),('7cdf959b-35cc-4fc4-85bd-091c057e8818','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','TEXT','a','2026-05-07 10:48:02',0,NULL,0,NULL,NULL),('84e62eef-acb9-48b3-92a7-7ddd99fec6f0','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','TEXT','a','2026-05-07 10:47:56',0,NULL,0,NULL,NULL),('861c784f-3623-47ee-9e4d-adbc4b47171f','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','TEXT','a','2026-05-07 10:48:00',0,NULL,0,NULL,NULL),('89c543bc-0dee-4529-9061-db05467096c6','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','TEXT','abc','2026-05-07 14:10:16',0,NULL,0,NULL,NULL),('8b46a181-cc42-4848-98b4-b26816c25382','1853096a-b11b-4ecb-9a03-e74cc5c22f18','54d52ba1-a244-42c9-8af7-f5154f008c1b','TEXT','aa','2026-05-07 14:16:13',0,NULL,0,NULL,NULL),('901bfe15-a2d3-4d36-801e-822da608e115','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','54d52ba1-a244-42c9-8af7-f5154f008c1b','TEXT','chào em','2026-05-07 11:00:03',0,NULL,0,NULL,NULL),('9fa88248-a0c1-4442-8857-ec584fe74c9a','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','TEXT','a','2026-05-07 10:48:02',0,NULL,0,NULL,NULL),('9fdcf33a-abae-4cbe-8372-4f8ebc75bcf7','1853096a-b11b-4ecb-9a03-e74cc5c22f18','54d52ba1-a244-42c9-8af7-f5154f008c1b','TEXT','lồ','2026-05-07 14:17:11',0,NULL,0,NULL,NULL),('a592bfea-fcf2-4d14-9ab3-4eb40d84d0ca','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','54d52ba1-a244-42c9-8af7-f5154f008c1b','TEXT','Hello','2026-05-07 10:36:41',0,NULL,0,NULL,NULL),('a7e952bc-bec3-4ecd-8d33-e9cf17418d08','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','TEXT','a','2026-05-07 10:48:03',0,NULL,0,NULL,NULL),('a7fa9f56-7309-4ab7-83ba-b233b3c3943e','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','54d52ba1-a244-42c9-8af7-f5154f008c1b','TEXT','aaa','2026-05-07 09:59:16',0,NULL,0,NULL,NULL),('abd75341-5008-4c98-bb9f-040484c06a4c','881b86ba-9d28-446a-a2fd-bfd454b5d68f','27c24d81-0c5d-44f9-9443-44199a13904a','TEXT','nnbk','2026-05-07 16:00:00',0,NULL,0,NULL,NULL),('acf82828-3e4b-4c0c-9788-6e7f96278503','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','54d52ba1-a244-42c9-8af7-f5154f008c1b','TEXT','a','2026-05-07 10:22:57',0,NULL,0,NULL,NULL),('b5ef1178-1962-468b-9c23-a49d7bcad439','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','TEXT','a','2026-05-07 10:48:00',0,NULL,0,NULL,NULL),('b7fbddba-fd06-4b4c-9878-5386175683f4','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','TEXT','a','2026-05-07 10:48:00',0,NULL,0,NULL,NULL),('bd5f787e-a4d0-4474-b07d-1174ccb38635','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','54d52ba1-a244-42c9-8af7-f5154f008c1b','TEXT','chào cậu','2026-05-07 10:24:49',0,NULL,0,NULL,NULL),('be8ee4ec-62a2-4cd0-9423-d1afea2803e5','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','TEXT','a','2026-05-07 10:48:02',0,NULL,0,NULL,NULL),('c00df335-7558-42db-8cb5-9a220c620ede','31055b4f-ebc4-4a95-9a51-114e2d580d56','07a28f6d-2f10-4813-bb49-1fcf927b01fe','TEXT','chó','2026-05-07 15:41:28',0,NULL,0,NULL,NULL),('c550a477-6d05-4cb4-bc24-a90f8ac1446d','1853096a-b11b-4ecb-9a03-e74cc5c22f18','54d52ba1-a244-42c9-8af7-f5154f008c1b','TEXT','fcfyc','2026-05-07 14:16:40',0,NULL,0,NULL,NULL),('c8928e30-ed98-48c4-8f9f-e82d8732edd5','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','TEXT','a','2026-05-07 10:47:58',0,NULL,0,NULL,NULL),('cc236b8a-a77f-4c95-bd6b-d5dc20a1756a','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','TEXT','a','2026-05-07 10:47:56',0,NULL,0,NULL,NULL),('cf7f35ae-ba17-465e-895d-cb9976959590','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','TEXT','a','2026-05-07 10:47:54',0,NULL,0,NULL,NULL),('cfe07159-3c66-4dfa-adb6-7017c9647183','881b86ba-9d28-446a-a2fd-bfd454b5d68f','27c24d81-0c5d-44f9-9443-44199a13904a','TEXT','âcc','2026-05-07 15:57:05',0,NULL,0,NULL,NULL),('d1fedb75-35d2-430b-83e9-675fcdbefa91','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','TEXT','a','2026-05-07 10:48:01',0,NULL,0,NULL,NULL),('d2a61373-bf0f-4669-b787-0b95d44824b3','1853096a-b11b-4ecb-9a03-e74cc5c22f18','da4846a4-a26f-42ec-824b-009f7221e3b5','TEXT','Ớn nen','2026-05-07 14:16:50',0,NULL,0,NULL,NULL),('d3862e59-6467-44cd-bcf2-207159df6a46','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','54d52ba1-a244-42c9-8af7-f5154f008c1b','TEXT','abc','2026-05-07 10:31:17',0,NULL,0,NULL,NULL),('db488197-4b8c-4d2c-a874-f31b37238843','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','TEXT','a','2026-05-07 10:48:01',0,NULL,0,NULL,NULL),('dccd1596-927d-49f6-bc62-bbca5001706e','881b86ba-9d28-446a-a2fd-bfd454b5d68f','7e9285fd-dbe4-4eaf-809d-d0043557a216','TEXT','abc','2026-05-07 15:57:56',0,NULL,0,NULL,NULL),('e2e7f9cb-4a74-4ada-9e17-294c732ce8df','881b86ba-9d28-446a-a2fd-bfd454b5d68f','27c24d81-0c5d-44f9-9443-44199a13904a','TEXT','kdnfks','2026-05-07 16:04:05',0,NULL,0,NULL,NULL),('ea2c127b-d604-4205-9cdb-5c3d9ae0df58','1853096a-b11b-4ecb-9a03-e74cc5c22f18','54d52ba1-a244-42c9-8af7-f5154f008c1b','TEXT','alo','2026-05-07 11:48:17',0,NULL,0,NULL,NULL),('ebe5a9cc-8afb-4165-a37a-7dfcfa6b5da4','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','TEXT','a','2026-05-07 10:48:04',0,NULL,0,NULL,NULL),('ed5292d9-3d75-4f40-84f2-01365e7d5ff7','3588ebc0-5f49-4112-b793-3975d091268d','61b01bab-16a8-4979-8a83-c8e871561da2','TEXT','Xin chào','2026-04-18 03:44:50',0,NULL,0,NULL,NULL),('f05dbb35-7c82-4066-9726-2ed597371d61','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','TEXT','a','2026-05-07 10:47:56',0,NULL,0,NULL,NULL),('f2c16e92-37bf-4ffb-bda0-900aecc2da88','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','TEXT','a','2026-05-07 10:47:57',0,NULL,0,NULL,NULL),('f40be617-0fb4-49df-8470-34f7ef5f0a4d','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','TEXT','dè','2026-05-07 14:09:14',0,NULL,0,NULL,NULL),('faa274f3-faae-4f84-82f2-ebb5ce668f03','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','54d52ba1-a244-42c9-8af7-f5154f008c1b','TEXT','check điện thoại','2026-05-07 12:03:08',0,NULL,0,NULL,NULL),('fad2cd92-be84-4b22-a672-3c0fd703a6b7','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','54d52ba1-a244-42c9-8af7-f5154f008c1b','TEXT','Test chức năng','2026-05-07 10:47:09',0,NULL,0,NULL,NULL),('fbd1d87b-1fa7-496a-88d8-047999629be2','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','54d52ba1-a244-42c9-8af7-f5154f008c1b','TEXT','abc','2026-05-07 10:27:37',0,NULL,0,NULL,NULL),('fd14d12f-4a05-4405-b8e3-87e4b896efb1','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','TEXT','a','2026-05-07 10:47:57',0,NULL,0,NULL,NULL),('fd7c1f42-030a-4957-a8d4-44cf4ec2005f','2b7deef1-c0d1-4348-aaf1-5cb7a8bab2f6','6e13b1b5-7ddd-45e7-b0e2-83514958895f','TEXT','a','2026-05-07 10:47:55',0,NULL,0,NULL,NULL);
/*!40000 ALTER TABLE `messages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` char(36) NOT NULL,
  `email` varchar(255) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `display_name` varchar(100) NOT NULL,
  `avatar_url` varchar(500) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `role` enum('ADMIN','USER') DEFAULT 'USER',
  `is_active` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES ('07a28f6d-2f10-4813-bb49-1fcf927b01fe','ltd@gmail.com','LTD','$2a$10$X.jIeLtJ0uE4mIMoITaEu./5Rlmc7rE9IMJNE7gyA3bgfd9/Ps9De','LTD',NULL,'2026-05-07 14:30:20','USER',1),('27c24d81-0c5d-44f9-9443-44199a13904a','abc1@gmail.com','abc1','$2a$10$YtZe5pIF4D6IaFX/D/OHre.cB0V4a87A8GcCtdjH7LM7.xOwyTNPS','abc1',NULL,'2026-05-07 15:55:49','USER',1),('54d52ba1-a244-42c9-8af7-f5154f008c1b','user1.5/7@gmail.com','user1.5/7','$2a$10$DxtKm1J4eaKSs3QKyHxajepSsptVRUM5WLgNuZ8e.FgQQuqbgAgxa','User1','https://b13bf59b0d8eb38a1056c3e7e7320536.r2.cloudflarestorage.com/chat-files/avatars/54d52ba1-a244-42c9-8af7-f5154f008c1b/61bd8870-cb61-46ab-bd22-7ea5b078de12.jpg','2026-05-07 09:39:58','USER',1),('591c0dd7-f2c5-47c2-a419-7254eae28a42','test10@gmail.com','test10','$2a$10$HxN6hsQKiYFelTpwuzp/levfmvAqSNog36MBmTDdd3kD3XXv3ND6q','test2',NULL,'2026-05-06 17:32:33','USER',1),('61b01bab-16a8-4979-8a83-c8e871561da2','test2@gmail.com','test2','$2a$10$CPwNdQqphwwycpYKKhtwAuWcE6ZNS9zq2NkYPu1DEozTaAmn1xGWK','test2','https://pub-c8f144bb53ad458e91df272891aa7a9e.r2.dev/avatars/61b01bab-16a8-4979-8a83-c8e871561da2/ba78ea73-23eb-4c4b-b036-157cf3741e3b.jpg','2026-04-18 03:33:15','USER',1),('6e13b1b5-7ddd-45e7-b0e2-83514958895f','user2.5/7@gmail.com','user2.5/7','$2a$10$HrTfuPR6D7EnRbEueV8OPerXA0QkS6Gul2pKG20Bj1tibgCAZQ/eS','user2.5/7',NULL,'2026-05-07 09:44:05','USER',1),('7e9285fd-dbe4-4eaf-809d-d0043557a216','abc2@gmail.com','abc2','$2a$10$binK/WGzh2uVZsJzyFXgJuPaVGoFzaTOdGf.EAv9TEpl47i60jViy','abc2',NULL,'2026-05-07 15:56:34','USER',1),('990fc321-1bf9-459a-b257-c3ba140e3d4c','test5/7@gmail.com','test5/7','$2a$10$cZKyOcIH5FmbGYWzz8NIsOc55ynwdi.Wz1m/I02JxcmSeN/LQZrRO','test5/7',NULL,'2026-05-06 17:35:03','USER',1),('a67cdec7-d531-4e78-bcbc-6db17912342f','test5/7-1@gmail.com','test5/7-1','$2a$10$gkeVL8OyILwty6e2a/ogC.CGfeN7ar5MZvwd0Rv0FnELZTXJrBElO','test5/7-1',NULL,'2026-05-06 17:36:32','USER',1),('b6d37a68-80fa-4706-8cc7-b7983413651b','test1@gmail.com','test1','$2a$10$dvPCx.9tcTuzPSiLdXz73.BPR.ZYtBkReC/v3Ckzq6NWEckZrVSti','test1','user.url','2026-04-18 03:33:05','USER',1),('d19332ac-ce70-4c05-9e30-78c56adfc646','test7@gmail.com','test7','$2a$10$oAG.6tlXoL34wI51oouAaOBsf86gexnIxmEjyVY4uOwbd42WpWLKO','test','user.url','2026-05-06 23:44:15','USER',1),('da4846a4-a26f-42ec-824b-009f7221e3b5','user3.5/7@gmail.com','user3.5/7','$2a$10$MYQ4e4CvURAaMdewZbnAuu7Spew74iOPks3vnSS2mSZ7Jcxs.E/aa','user3.5/7',NULL,'2026-05-07 11:11:49','USER',1),('e7960cee-2b58-4363-b145-264300c78dbd','test8@gmail.com','test8','$2a$10$T/oNLRVRUg4Q31lU4yH.wezNPn27/LfhJoUrdNG5pcdGLlH089LI.','test',NULL,'2026-05-06 23:53:56','USER',1);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-12 11:01:01
