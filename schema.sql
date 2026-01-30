-- Campus Resource Coordination System (CRCS) - MySQL Schema
-- Database: crcs_db
-- Run this script to create the schema (e.g. when not using JPA ddl-auto=create/update).

CREATE DATABASE IF NOT EXISTS crcs_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE crcs_db;

-- ---------------------------------------------------------------------------
-- Auth Service
-- ---------------------------------------------------------------------------

-- Users (auth credentials and role)
CREATE TABLE IF NOT EXISTS users (
  id       VARCHAR(36)  NOT NULL PRIMARY KEY,
  email    VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  role     VARCHAR(50)  NOT NULL
);

-- Refresh tokens for JWT refresh
CREATE TABLE IF NOT EXISTS refresh_tokens (
  id         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id    VARCHAR(36)  NOT NULL,
  token      VARCHAR(500) NOT NULL UNIQUE,
  expires_at DATETIME(6)  NOT NULL,
  created_at DATETIME(6)  NOT NULL,
  INDEX idx_refresh_tokens_user_id (user_id),
  INDEX idx_refresh_tokens_token (token)
);

-- ---------------------------------------------------------------------------
-- User Service
-- ---------------------------------------------------------------------------

-- User profiles (extended profile data, keyed by auth user id)
CREATE TABLE IF NOT EXISTS user_profiles (
  user_id          VARCHAR(36)   NOT NULL PRIMARY KEY,
  email            VARCHAR(255)  NOT NULL,
  name             VARCHAR(255)  NULL,
  role             VARCHAR(50)   NULL,
  profile_picture  VARCHAR(500)  NULL,
  bio              VARCHAR(1000) NULL,
  phone_number     VARCHAR(20)   NULL,
  is_email_verified TINYINT(1)   DEFAULT 0,
  is_phone_verified TINYINT(1)   DEFAULT 0,
  is_active        TINYINT(1)   DEFAULT 1,
  created_at       DATETIME(6)   NOT NULL,
  updated_at       DATETIME(6)   NOT NULL
);

-- ---------------------------------------------------------------------------
-- Resource Service
-- ---------------------------------------------------------------------------

-- Resources (rooms, labs, equipment)
CREATE TABLE IF NOT EXISTS resources (
  id                 VARCHAR(36)   NOT NULL PRIMARY KEY,
  name               VARCHAR(255)  NOT NULL,
  type               VARCHAR(50)   NULL,
  description        VARCHAR(500)  NULL,
  status             VARCHAR(50)   NOT NULL DEFAULT 'AVAILABLE',
  location           VARCHAR(255)  NULL,
  capacity           INT           NULL,
  owner_id           VARCHAR(36)   NULL,
  responsible_person VARCHAR(255)  NULL,
  created_at         DATETIME(6)   NOT NULL,
  updated_at         DATETIME(6)   NOT NULL,
  INDEX idx_resources_type (type),
  INDEX idx_resources_status (status),
  INDEX idx_resources_owner_id (owner_id)
);

-- ---------------------------------------------------------------------------
-- Booking Service
-- ---------------------------------------------------------------------------

-- Bookings
CREATE TABLE IF NOT EXISTS bookings (
  id          VARCHAR(36)   NOT NULL PRIMARY KEY,
  user_id     VARCHAR(36)   NOT NULL,
  resource_id VARCHAR(36)   NOT NULL,
  start_time  DATETIME(6)   NOT NULL,
  end_time    DATETIME(6)   NOT NULL,
  status      VARCHAR(50)   NOT NULL DEFAULT 'PENDING',
  purpose     VARCHAR(500)  NULL,
  created_at  DATETIME(6)   NOT NULL,
  updated_at  DATETIME(6)   NOT NULL,
  INDEX idx_bookings_user_id (user_id),
  INDEX idx_bookings_resource_id (resource_id),
  INDEX idx_bookings_start_time (start_time),
  INDEX idx_bookings_end_time (end_time),
  INDEX idx_bookings_status (status)
);
