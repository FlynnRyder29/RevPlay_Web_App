-- ============================================================
-- V2__add_private_visibility.sql
-- Fix: Java Song.Visibility enum has PRIVATE but DB ENUM
-- only has PUBLIC, UNLISTED. Adding PRIVATE to prevent
-- SQL error when artist sets a song to PRIVATE.
-- ============================================================

ALTER TABLE songs
    MODIFY COLUMN visibility ENUM('PUBLIC', 'UNLISTED', 'PRIVATE')
    NOT NULL DEFAULT 'PUBLIC';