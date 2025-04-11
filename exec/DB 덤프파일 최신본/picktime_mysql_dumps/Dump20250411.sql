-- MySQL dump 10.13  Distrib 8.0.38, for Win64 (x86_64)
--
-- Host: j12b101.p.ssafy.io    Database: picktime
-- ------------------------------------------------------
-- Server version	8.0.41

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
-- Table structure for table `chords`
--

DROP TABLE IF EXISTS `chords`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chords` (
  `chord_id` int NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`chord_id`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chords`
--

LOCK TABLES `chords` WRITE;
/*!40000 ALTER TABLE `chords` DISABLE KEYS */;
INSERT INTO `chords` VALUES (1),(2),(3),(4),(5),(6),(7),(8),(9),(10),(11),(12),(13),(14),(15),(16),(17),(18),(19),(20),(21),(22),(23),(24),(25),(26),(27),(28),(29),(30);
/*!40000 ALTER TABLE `chords` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `completed_songs`
--

DROP TABLE IF EXISTS `completed_songs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `completed_songs` (
  `completed_song_id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `score` int NOT NULL,
  `song_id` int NOT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`completed_song_id`),
  KEY `FKjmlj3jm1a3nye17b68p2u566j` (`song_id`),
  KEY `FKqa8vx6l57lrb6qmfe7nj3f4x4` (`user_id`),
  CONSTRAINT `FKjmlj3jm1a3nye17b68p2u566j` FOREIGN KEY (`song_id`) REFERENCES `songs` (`song_id`),
  CONSTRAINT `FKqa8vx6l57lrb6qmfe7nj3f4x4` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=85 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `completed_songs`
--

LOCK TABLES `completed_songs` WRITE;
/*!40000 ALTER TABLE `completed_songs` DISABLE KEYS */;
INSERT INTO `completed_songs` VALUES (1,'2025-03-31 08:33:31.700624',2,4,13),(2,'2025-03-31 08:36:48.537748',3,4,15),(3,'2025-04-03 06:32:52.421511',3,13,15),(4,'2025-04-03 11:40:40.298396',3,11,15),(5,'2025-04-03 13:11:31.746655',3,12,15),(6,'2025-04-03 13:17:09.998400',3,5,15),(7,'2025-04-03 13:27:22.990386',3,6,15),(8,'2025-04-04 06:56:40.941326',2,11,14),(9,'2024-10-14 00:00:00.000000',1,16,15),(10,'2024-09-11 00:00:00.000000',2,38,15),(11,'2024-12-17 00:00:00.000000',3,12,15),(12,'2024-09-03 00:00:00.000000',1,17,15),(13,'2024-10-10 00:00:00.000000',2,24,15),(14,'2025-03-05 00:00:00.000000',3,27,15),(15,'2024-07-10 00:00:00.000000',3,13,15),(16,'2024-10-16 00:00:00.000000',3,5,15),(17,'2025-03-13 00:00:00.000000',3,17,15),(18,'2024-12-27 00:00:00.000000',1,10,15),(19,'2025-03-09 00:00:00.000000',3,11,15),(20,'2025-03-21 00:00:00.000000',2,12,15),(21,'2024-10-30 00:00:00.000000',1,13,15),(22,'2024-12-26 00:00:00.000000',1,26,15),(23,'2024-07-28 00:00:00.000000',1,11,15),(24,'2024-06-19 00:00:00.000000',1,5,15),(25,'2024-09-15 00:00:00.000000',3,15,15),(26,'2025-03-20 00:00:00.000000',3,32,15),(27,'2024-10-02 00:00:00.000000',1,20,15),(28,'2024-09-12 00:00:00.000000',3,36,15),(29,'2024-07-17 00:00:00.000000',1,5,15),(30,'2024-12-03 00:00:00.000000',2,12,15),(31,'2024-08-21 00:00:00.000000',1,6,15),(32,'2024-11-05 00:00:00.000000',1,20,15),(33,'2025-03-28 00:00:00.000000',3,7,15),(34,'2024-08-03 00:00:00.000000',2,10,15),(35,'2025-02-14 00:00:00.000000',1,34,15),(36,'2024-11-26 00:00:00.000000',2,11,15),(37,'2025-03-22 00:00:00.000000',1,21,15),(38,'2025-01-23 00:00:00.000000',3,12,15),(39,'2024-11-08 00:00:00.000000',3,13,15),(40,'2025-01-14 00:00:00.000000',3,26,15),(41,'2024-09-07 00:00:00.000000',1,21,15),(42,'2024-09-25 00:00:00.000000',3,29,15),(43,'2024-06-22 00:00:00.000000',2,25,15),(44,'2024-12-04 00:00:00.000000',2,26,15),(45,'2024-08-26 00:00:00.000000',2,14,15),(46,'2024-11-04 00:00:00.000000',1,13,15),(47,'2024-10-11 00:00:00.000000',3,13,15),(48,'2025-01-08 00:00:00.000000',1,22,15),(49,'2024-07-31 00:00:00.000000',3,10,15),(50,'2024-12-23 00:00:00.000000',1,34,15),(51,'2024-12-18 00:00:00.000000',2,14,15),(52,'2024-11-24 00:00:00.000000',1,6,15),(53,'2024-11-13 00:00:00.000000',3,11,15),(54,'2025-02-08 00:00:00.000000',2,13,15),(55,'2025-01-12 00:00:00.000000',2,28,15),(56,'2025-02-10 00:00:00.000000',1,15,15),(57,'2025-01-22 00:00:00.000000',1,12,15),(58,'2024-07-12 00:00:00.000000',1,7,15),(59,'2024-11-28 00:00:00.000000',3,30,15),(60,'2024-09-14 00:00:00.000000',1,34,15),(61,'2025-02-09 00:00:00.000000',3,6,15),(62,'2024-09-06 00:00:00.000000',2,22,15),(63,'2024-08-15 00:00:00.000000',3,11,15),(64,'2025-03-23 00:00:00.000000',3,22,15),(65,'2025-01-17 00:00:00.000000',3,10,15),(66,'2024-08-23 00:00:00.000000',2,23,15),(67,'2024-08-10 00:00:00.000000',2,16,15),(68,'2025-04-02 00:00:00.000000',2,7,15),(69,'2024-08-25 00:00:00.000000',3,17,15),(70,'2024-10-01 00:00:00.000000',3,32,15),(71,'2024-09-04 00:00:00.000000',1,25,15),(72,'2024-08-09 00:00:00.000000',1,18,15),(73,'2025-01-15 00:00:00.000000',2,13,15),(74,'2024-07-21 00:00:00.000000',1,15,15),(75,'2025-03-04 00:00:00.000000',3,34,15),(76,'2025-04-03 00:00:00.000000',1,5,15),(77,'2024-10-25 00:00:00.000000',1,12,15),(78,'2025-03-25 00:00:00.000000',2,12,15),(79,'2024-07-08 00:00:00.000000',3,38,15),(80,'2024-12-21 00:00:00.000000',2,24,15),(81,'2025-04-10 06:54:38.339336',1,39,14),(82,'2025-04-10 14:41:08.339165',1,12,14),(83,'2025-04-10 14:50:34.362191',1,12,14),(84,'2025-04-10 14:55:33.816340',1,39,15);
/*!40000 ALTER TABLE `completed_songs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `completed_steps`
--

DROP TABLE IF EXISTS `completed_steps`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `completed_steps` (
  `completed_step_id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `score` int NOT NULL,
  `step_id` int NOT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`completed_step_id`),
  KEY `FKr8c57pybyvuq9b3pqplbo10l3` (`step_id`),
  KEY `FKq77cqgx5lys4fes7jpedl700w` (`user_id`),
  CONSTRAINT `FKq77cqgx5lys4fes7jpedl700w` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `FKr8c57pybyvuq9b3pqplbo10l3` FOREIGN KEY (`step_id`) REFERENCES `steps` (`step_id`)
) ENGINE=InnoDB AUTO_INCREMENT=153 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `completed_steps`
--

LOCK TABLES `completed_steps` WRITE;
/*!40000 ALTER TABLE `completed_steps` DISABLE KEYS */;
INSERT INTO `completed_steps` VALUES (1,'2025-03-28 15:13:04.417916',3,1,16),(2,'2025-03-28 15:13:08.118506',3,2,16),(3,'2025-03-28 15:13:11.051690',3,3,16),(4,'2025-03-28 15:13:15.174566',3,4,16),(61,'2025-04-09 11:14:26.432695',0,1,14),(63,'2025-04-09 11:46:19.116147',0,1,14),(64,'2025-04-09 11:46:44.763544',0,2,14),(65,'2025-04-09 11:47:14.979741',0,3,14),(66,'2025-04-09 11:48:00.759190',0,1,14),(67,'2025-04-09 11:48:24.373473',0,1,14),(68,'2025-04-09 11:53:26.204322',0,2,14),(69,'2025-04-09 11:53:43.383427',0,2,14),(70,'2025-04-09 12:11:53.773780',0,2,14),(71,'2025-04-09 12:12:46.828389',0,3,14),(76,'2025-04-10 10:40:30.405656',1,3,14),(77,'2025-04-10 10:50:54.591471',1,2,14),(82,'2025-04-10 13:03:54.184977',1,3,14),(83,'2025-04-10 13:04:19.894303',1,3,14),(84,'2025-04-10 13:09:10.322266',1,3,14),(85,'2025-04-10 13:12:06.659870',1,3,14),(86,'2025-04-10 13:18:59.684753',1,3,14),(91,'2025-04-10 14:35:59.458042',1,3,14),(92,'2025-04-10 14:36:21.532937',1,3,14),(93,'2025-04-10 14:37:03.702710',1,4,14),(94,'2025-04-10 14:45:36.783476',1,7,14),(95,'2025-04-10 14:46:21.547304',1,8,14),(97,'2025-04-10 14:49:23.687195',1,8,14),(107,'2025-04-10 15:32:04.902935',1,9,15),(145,'2025-04-10 23:33:55.281729',3,1,15),(146,'2025-04-10 23:33:57.428062',3,2,15),(147,'2025-04-10 23:33:59.174961',3,3,15),(148,'2025-04-10 23:34:00.617179',3,4,15),(149,'2025-04-10 23:54:11.609898',1,5,15),(150,'2025-04-10 23:55:03.001161',1,6,15),(151,'2025-04-10 23:55:28.214644',1,7,15),(152,'2025-04-10 23:56:20.462960',1,8,15);
/*!40000 ALTER TABLE `completed_steps` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `songs`
--

DROP TABLE IF EXISTS `songs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `songs` (
  `song_id` int NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`song_id`)
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `songs`
--

LOCK TABLES `songs` WRITE;
/*!40000 ALTER TABLE `songs` DISABLE KEYS */;
INSERT INTO `songs` VALUES (4),(5),(6),(7),(8),(9),(10),(11),(12),(13),(14),(15),(16),(17),(18),(19),(20),(21),(22),(23),(24),(25),(26),(27),(28),(29),(30),(31),(32),(33),(34),(35),(36),(37),(38),(39);
/*!40000 ALTER TABLE `songs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stages`
--

DROP TABLE IF EXISTS `stages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stages` (
  `stage_id` int NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`stage_id`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stages`
--

LOCK TABLES `stages` WRITE;
/*!40000 ALTER TABLE `stages` DISABLE KEYS */;
INSERT INTO `stages` VALUES (1,'Am Em'),(2,'G C'),(3,'Am Dm G C'),(4,'A E'),(5,'G E A C'),(6,'Am Dm'),(7,'G D Am Dm'),(8,'F Bm'),(9,'G Bm C F'),(10,'E7 A7'),(11,'G E7 A7 D'),(12,'B7 D7'),(13,'E D7 A B7'),(14,'C7 G7'),(15,'C C7 G G7'),(16,'F#m C#m'),(17,'A F#m C#m E'),(18,'Bm7 F#m7'),(19,'D F#m7 Bm7 G'),(20,'Em7 Am7'),(21,'G Em7 Am7 D'),(22,'Dsus4 Asus4'),(23,'D Dsus4 A Asus4'),(24,'Cadd9 Gadd9'),(25,'C Cadd9 G Gadd9'),(26,'Fmaj7 Emaj7'),(27,'F Fmaj7 E Emaj7'),(28,'C#m7 G#m7'),(29,'E C#m7 B G#m7');
/*!40000 ALTER TABLE `stages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `steps`
--

DROP TABLE IF EXISTS `steps`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `steps` (
  `step_id` int NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `step_number` int NOT NULL,
  `step_type` int NOT NULL,
  `chord_id` int DEFAULT NULL,
  `song_id` int DEFAULT NULL,
  `stage_id` int NOT NULL,
  PRIMARY KEY (`step_id`),
  KEY `FK4p81mhict7slveuths345boi9` (`chord_id`),
  KEY `FK6g3kr8tfr4fsljr2b2eah9fn8` (`song_id`),
  KEY `FKntgxt7jbt6li2i970hq2nhrd` (`stage_id`),
  CONSTRAINT `FK4p81mhict7slveuths345boi9` FOREIGN KEY (`chord_id`) REFERENCES `chords` (`chord_id`),
  CONSTRAINT `FK6g3kr8tfr4fsljr2b2eah9fn8` FOREIGN KEY (`song_id`) REFERENCES `songs` (`song_id`),
  CONSTRAINT `FKntgxt7jbt6li2i970hq2nhrd` FOREIGN KEY (`stage_id`) REFERENCES `stages` (`stage_id`)
) ENGINE=InnoDB AUTO_INCREMENT=76 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `steps`
--

LOCK TABLES `steps` WRITE;
/*!40000 ALTER TABLE `steps` DISABLE KEYS */;
INSERT INTO `steps` VALUES (1,'Am 코드 연습',1,1,7,NULL,1),(2,'Dm 코드 연습',2,1,8,NULL,1),(3,'Am Dm 코드 전환 연습',3,2,NULL,NULL,1),(4,'Am Dm',4,3,NULL,39,1),(5,'G 코드 연습',1,1,1,NULL,2),(6,'C 코드 연습',2,1,4,NULL,2),(7,'G C 코드 전환 연습',3,2,NULL,NULL,2),(8,'G C',4,3,NULL,39,2),(9,'Am Dm C G',1,3,NULL,39,3),(11,'A 코드 연습',1,1,5,NULL,4),(12,'E 코드 연습',2,1,6,NULL,4),(13,'A E 코드 전환',3,2,NULL,NULL,4),(14,'A E',4,3,NULL,36,4),(15,'G E A C',1,3,NULL,6,5),(16,'Am 코드 연습',1,1,7,NULL,6),(17,'Dm 코드 연습',2,1,8,NULL,6),(18,'Am Dm 코드 전환',3,2,NULL,NULL,6),(19,'Am Dm',4,3,NULL,17,6),(20,'G D Am Dm',1,3,NULL,8,7),(21,'F 코드 연습',1,1,9,NULL,8),(22,'Bm 코드 연습',2,1,10,NULL,8),(23,'F Bm 코드 전환',3,2,NULL,NULL,8),(24,'F Bm',4,3,NULL,17,8),(25,'G Bm C F',1,3,NULL,34,9),(26,'E7 코드 연습',1,1,11,NULL,10),(27,'A7 코드 연습',2,1,12,NULL,10),(28,'E7 A7 코드 전환',3,2,NULL,NULL,10),(29,'E7 A7',4,3,NULL,5,10),(30,'G E7 A7 D',1,3,NULL,35,11),(31,'B7 코드 연습',1,1,13,NULL,12),(32,'D7 코드 연습',2,1,14,NULL,12),(33,'B7 D7 코드 전환',3,2,NULL,NULL,12),(34,'B7 D7',4,3,NULL,34,12),(35,'E D7 A B7',1,3,NULL,25,13),(36,'C7 코드 연습',1,1,15,NULL,14),(37,'G7 코드 연습',2,1,16,NULL,14),(38,'C7 G7 코드 전환',3,2,NULL,NULL,14),(39,'C7 G7',4,3,NULL,12,14),(40,'C C7 G G7',1,3,NULL,39,15),(41,'F#m 코드 연습',1,1,17,NULL,16),(42,'C#m 코드 연습',2,1,18,NULL,16),(43,'F#m C#m 코드 전환',3,2,NULL,NULL,16),(44,'F#m C#m',4,3,NULL,16,16),(45,'A F#m C#m E',1,3,NULL,18,17),(46,'Bm7 코드 연습',1,1,19,NULL,18),(47,'F#m7 코드 연습',2,1,20,NULL,18),(48,'Bm7 F#m7 코드 전환',3,2,NULL,NULL,18),(49,'Bm7 F#m7',4,3,NULL,10,18),(50,'D F#m7 Bm7 G',1,3,NULL,12,19),(51,'Em7 코드 연습',1,1,21,NULL,20),(52,'Am7 코드 연습',2,1,22,NULL,20),(53,'Em7 Am7 코드 전환',3,2,NULL,NULL,20),(54,'Em7 Am7',4,3,NULL,12,20),(55,'G Em7 Am7 D',1,3,NULL,22,21),(56,'Dsus4 코드 연습',1,1,23,NULL,22),(57,'Asus4 코드 연습',2,1,24,NULL,22),(58,'Dsus4 Asus4 코드 전환',3,2,NULL,NULL,22),(59,'Dsus4 Asus4',4,3,NULL,30,22),(60,'D Dsus4 A Asus4',1,3,NULL,31,23),(61,'Cadd9 코드 연습',1,1,25,NULL,24),(62,'Gadd9 코드 연습',2,1,26,NULL,24),(63,'Cadd9 Gadd9 코드 전환',3,2,NULL,NULL,24),(64,'Cadd9 Gadd9',4,3,NULL,11,24),(65,'C Cadd9 G Gadd9',1,3,NULL,33,25),(66,'Fmaj7 코드 연습',1,1,27,NULL,26),(67,'Emaj7 코드 연습',2,1,28,NULL,26),(68,'Fmaj7 Emaj7 코드 전환',3,2,NULL,NULL,26),(69,'Fmaj7 Emaj7',4,3,NULL,35,26),(70,'F Fmaj7 E Emaj7',1,3,NULL,37,27),(71,'C#m7 코드 연습',1,1,29,NULL,28),(72,'G#m7 코드 연습',2,1,30,NULL,28),(73,'C#m7 G#m7 코드 전환',3,2,NULL,NULL,28),(74,'C#m7 G#m7',4,3,NULL,39,28),(75,'E C#m7 B G#m7',1,3,NULL,39,29);
/*!40000 ALTER TABLE `steps` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `testTable`
--

DROP TABLE IF EXISTS `testTable`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `testTable` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(20) NOT NULL,
  `ouccupation` varchar(20) DEFAULT NULL,
  `height` smallint DEFAULT NULL,
  `profile` text,
  `date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `testTable`
--

LOCK TABLES `testTable` WRITE;
/*!40000 ALTER TABLE `testTable` DISABLE KEYS */;
/*!40000 ALTER TABLE `testTable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `is_active` bit(1) NOT NULL,
  `level` int NOT NULL,
  `name` varchar(21) NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  `role` enum('ROLE_ADMIN','ROLE_USER') NOT NULL,
  `username` varchar(255) NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `UKr43af9ap4edm43mmtq01oddj6` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'2025-03-25 00:39:01.678473',_binary '',1,'asdasas','$2a$10$zug5AtwxRd9eFpf0P5CSsOkZ61.tu0rUncwnzgXI/cGqrOYyqxCmK','ROLE_USER','asdasd'),(2,'2025-03-25 00:49:06.655351',_binary '',1,'asdaa','$2a$10$CY.0.uuX48sFr3sZcIwm6eysqRdEIixLhnnMHAEMJJJs0hjchM9C6','ROLE_USER','sdasdas'),(3,'2025-03-25 00:58:41.604575',_binary '',1,'hello','$2a$10$vYWmiKT0H3k8MMOUleYunusE/uOapMlBHWyEONZxM06hDLqP87LSu','ROLE_USER','hello@naver.com'),(4,'2025-03-25 01:26:49.579720',_binary '',1,'hello','$2a$10$L4vNTZjy6W6v0jYBGCBOPewVFcB.TyAHcHL6kpgwJ7Bg1s697InNe','ROLE_USER','hello2@naver.com'),(5,'2025-03-25 01:33:33.759729',_binary '',1,'hello','$2a$10$bSuLvQJg882OVakPbm0d7OFMAVC0jn7B1T7X6lAwUh4ilWfrroM8G','ROLE_USER','test99@naver.com'),(6,'2025-03-25 07:49:02.060668',_binary '',1,'성근이잘생겼','$2a$10$s2rG6IkbAujaJ8vJdr1tZe1n.yWOkr.LLLqCLOti4a6OUAhyTBSO.','ROLE_USER','prince@naver.com'),(7,'2025-03-25 08:10:44.097027',_binary '',1,'hihi','$2a$10$0evWTV01H9Y.wthuNLXtwOeIaLCDnSUL3bHaXp29pT7qjMp30lUPO','ROLE_USER','test96@naver.com'),(8,'2025-03-25 08:26:44.527801',_binary '',1,'dashd','$2a$10$I18.Ky5I9Y/vrovN4/QSROCYOxIF4yxqFoeXE6GKfussCG2sacwGm','ROLE_USER','qwer@naver.com'),(9,'2025-03-25 12:15:44.572115',_binary '',1,'ggg','$2a$10$SED4Q0NE2U.E6Cv/Sr.Lm.lbJ.WDD108bIMIKIQoUzrovDpkDP9YK','ROLE_USER','ggg@ggg.com'),(10,'2025-03-25 13:02:16.776457',_binary '',1,'test95','$2a$10$s50gMcRt3dVyHpOKCsPXXuSNrDR66kHrfLYEGOsklPUZcmwlkQqo.','ROLE_USER','test95@naver.com'),(11,'2025-03-25 15:00:53.240400',_binary '',1,'자고싶다','$2a$10$efkQadwx81C6jylEC80Ck.GfimkJxs1YSTd5NF.7WJyZ7SA8aAsZ.','ROLE_USER','test94@naver.com'),(12,'2025-03-28 01:58:57.508972',_binary '',1,'naver\n','$2a$10$O1tivJa3/S10pUdRIGHwyeQrLoxxaeEgYNZcelQcZK8Isi0yg2SrK','ROLE_USER','99minj0731@naver.com'),(13,'2025-03-28 06:13:23.725576',_binary '',1,'박성근','$2a$10$IpyzvGfr1Wxz/R8HK/pHAeXGPqPCVA9LLj703rlDDnR5rIy6fz58i','ROLE_USER','test3'),(14,'2025-03-28 06:30:45.750746',_binary '',1,'박성근','$2a$10$13gjyaF1cQ4zJSkgZ9qgaus8K7sdrvRA68mJuoe3hD0rOY5n7wbsu','ROLE_USER','test4'),(15,'2025-03-28 14:46:14.533344',_binary '',1,'기리니','$2a$10$QwtaVX3obPY0zbaJk0CT.Of2iuCII0Jb448GzPzJr2WUY7/F48196','ROLE_USER','min'),(16,'2025-03-28 15:12:27.120170',_binary '',1,'박성근','$2a$10$LIH8mdfIdXjan1qeINtq4.mBzQAoDlp42IY0JUwQI4bMNrGhQCHLO','ROLE_USER','test5'),(17,'2025-04-03 13:07:47.812601',_binary '',1,'박성근','$2a$10$.hewGVBF1gv1dBnBRbKLPOk6G0yHUmyinHNPd86Ai1WkM93d7Z2vq','ROLE_USER','test10');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `verifications`
--

DROP TABLE IF EXISTS `verifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `verifications` (
  `verification_id` int NOT NULL AUTO_INCREMENT,
  `expiration_time` datetime(6) NOT NULL,
  `username` varchar(255) NOT NULL,
  `verification_number` varchar(255) NOT NULL,
  PRIMARY KEY (`verification_id`),
  UNIQUE KEY `UKc8prtdhl9htg98c9tbxmfes7k` (`username`),
  UNIQUE KEY `UKn9a9yrtlwu89lkri9eh2spjmd` (`verification_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `verifications`
--

LOCK TABLES `verifications` WRITE;
/*!40000 ALTER TABLE `verifications` DISABLE KEYS */;
/*!40000 ALTER TABLE `verifications` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-04-11  9:29:28
