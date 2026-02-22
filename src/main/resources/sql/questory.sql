CREATE SCHEMA `questory`;

USE `questory`;

CREATE TABLE member (
    member_id   BIGINT NOT NULL AUTO_INCREMENT,
    email       VARCHAR(255) NOT NULL,
    nickname    VARCHAR(50)  NOT NULL,
    status      ENUM('NORMAL','LOCKED','SOFT_DELETE') NOT NULL DEFAULT 'NORMAL',
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at  DATETIME NULL,
    PRIMARY KEY (member_id),
    UNIQUE KEY uk_member_email (email),
    UNIQUE KEY uk_member_nickname (nickname)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE member_password_credentials (
     member_id            BIGINT NOT NULL,
     password_hash        VARCHAR(255) NOT NULL,
     password_updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
     failed_login_count   INT NOT NULL DEFAULT 0,
     last_failed_login_at DATETIME NULL,
     locked_until         DATETIME NULL,
     PRIMARY KEY (member_id),
     CONSTRAINT fk_mpc_member
         FOREIGN KEY (member_id) REFERENCES member(member_id)
             ON DELETE CASCADE
             ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE member_oauth_accounts (
    member_identities_id BIGINT NOT NULL AUTO_INCREMENT,
    member_id            BIGINT NOT NULL,
    provider             ENUM('KAKAO','GOOGLE','NAVER') NOT NULL,
    provider_member_id   VARCHAR(128) NOT NULL,
    email_verified       TINYINT(1) NOT NULL DEFAULT 0,
    linked_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    access_token         TEXT NULL,
    refresh_token        TEXT NULL,
    token_expires_at     DATETIME NULL,
    PRIMARY KEY (member_identities_id),
    UNIQUE KEY uk_provider_provider_member (provider, provider_member_id),
    KEY idx_member_id (member_id),

    CONSTRAINT fk_mi_member
       FOREIGN KEY (member_id) REFERENCES member(member_id)
           ON DELETE CASCADE
           ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;
