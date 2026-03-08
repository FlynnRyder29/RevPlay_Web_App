-- ============================================================
-- Artist Upgrade Requests
-- ============================================================

CREATE TABLE artist_requests (
                                 id              BIGINT          NOT NULL AUTO_INCREMENT,
                                 user_id         BIGINT          NOT NULL,
                                 artist_name     VARCHAR(150)    NOT NULL,
                                 genre           VARCHAR(100),
                                 reason          TEXT,
                                 status          ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
                                 admin_note      TEXT,
                                 reviewed_by     BIGINT,
                                 reviewed_at     DATETIME,
                                 created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 PRIMARY KEY (id),
                                 CONSTRAINT fk_ar_user_id      FOREIGN KEY (user_id)     REFERENCES users (id) ON DELETE CASCADE,
                                 CONSTRAINT fk_ar_reviewed_by  FOREIGN KEY (reviewed_by) REFERENCES users (id) ON DELETE SET NULL
);

CREATE INDEX idx_ar_user_id ON artist_requests (user_id);
CREATE INDEX idx_ar_status  ON artist_requests (status);