CREATE SCHEMA `questory`;

USE `questory`;

CREATE TABLE title (
    title_id    BIGINT NOT NULL AUTO_INCREMENT,
    name        VARCHAR(50) NOT NULL,

    PRIMARY KEY (title_id),
    UNIQUE KEY uk_title_name (name)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE member (
    member_id               BIGINT NOT NULL AUTO_INCREMENT,
    email                   VARCHAR(255) NOT NULL,
    nickname                VARCHAR(50)  NOT NULL,
    total_exp               BIGINT NOT NULL DEFAULT 0,
    representative_title_id BIGINT NULL,
    status                  ENUM('NORMAL','LOCKED','SOFT_DELETE') NOT NULL DEFAULT 'NORMAL',
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at              DATETIME NULL,

    PRIMARY KEY (member_id),
    UNIQUE KEY uk_member_email (email),

    CONSTRAINT fk_member_representative_title
        FOREIGN KEY (representative_title_id) REFERENCES title(title_id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
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
    member_oauth_accounts_id BIGINT NOT NULL AUTO_INCREMENT,
    member_id            BIGINT NOT NULL,
    provider             ENUM('KAKAO','GOOGLE','NAVER') NOT NULL,
    provider_member_id   VARCHAR(128) NOT NULL,

    PRIMARY KEY (member_oauth_accounts_id),
    UNIQUE KEY uk_provider_provider_member (provider, provider_member_id),
    KEY idx_member_id (member_id),

    CONSTRAINT fk_mi_member
       FOREIGN KEY (member_id) REFERENCES member(member_id)
           ON DELETE CASCADE
           ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE member_title (
    member_id BIGINT NOT NULL,
    title_id BIGINT NOT NULL,
    acquired_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (member_id, title_id),
    KEY idx_member_title_title_id (title_id),
    CONSTRAINT fk_member_title_member
        FOREIGN KEY (member_id) REFERENCES member(member_id)
            ON DELETE CASCADE
            ON UPDATE CASCADE,
    CONSTRAINT fk_member_title_title
        FOREIGN KEY (title_id) REFERENCES title(title_id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE friend (
    friend_id   BIGINT NOT NULL AUTO_INCREMENT,
    member_a_id BIGINT NOT NULL,
    member_b_id BIGINT NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (friend_id),
    UNIQUE KEY uk_friendship_pair (member_a_id, member_b_id),

    KEY idx_friendship_a (member_a_id, created_at),
    KEY idx_friendship_b (member_b_id, created_at),

    CONSTRAINT fk_friendship_a FOREIGN KEY (member_a_id) REFERENCES member(member_id),
    CONSTRAINT fk_friendship_b FOREIGN KEY (member_b_id) REFERENCES member(member_id),

    CONSTRAINT chk_friendship_order CHECK (member_a_id < member_b_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE friend_request (
    friend_request_id   BIGINT NOT NULL AUTO_INCREMENT,
    sender_id           BIGINT NOT NULL,
    receiver_id         BIGINT NOT NULL,
    status              ENUM('PENDING','ACCEPTED','REJECTED','CANCELED') NOT NULL DEFAULT 'PENDING',
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    responded_at        DATETIME NULL,

    PRIMARY KEY (friend_request_id),

    KEY idx_receiver_status_created (receiver_id, status, created_at),
    KEY idx_sender_status_created   (sender_id, status, created_at),

    CONSTRAINT fk_fr_sender   FOREIGN KEY (sender_id)   REFERENCES member(member_id),
    CONSTRAINT fk_fr_receiver FOREIGN KEY (receiver_id) REFERENCES member(member_id),

    CONSTRAINT chk_fr_not_self CHECK (sender_id <> receiver_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;
