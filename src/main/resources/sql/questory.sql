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

    pair_a BIGINT GENERATED ALWAYS AS (LEAST(sender_id, receiver_id)) STORED,
    pair_b BIGINT GENERATED ALWAYS AS (GREATEST(sender_id, receiver_id)) STORED,

    pending_flag TINYINT GENERATED ALWAYS AS (
        CASE WHEN status = 'PENDING' THEN 1 ELSE NULL END
    ) STORED,

    PRIMARY KEY (friend_request_id),

    KEY idx_receiver_status_created (receiver_id, status, created_at),
    KEY idx_sender_status_created   (sender_id, status, created_at),
    KEY idx_pair_pending (pair_a, pair_b, pending_flag),

    UNIQUE KEY uk_fr_pair_pending (pair_a, pair_b, pending_flag),

    CONSTRAINT fk_fr_sender   FOREIGN KEY (sender_id)   REFERENCES member(member_id),
    CONSTRAINT fk_fr_receiver FOREIGN KEY (receiver_id) REFERENCES member(member_id),

    CONSTRAINT chk_fr_not_self CHECK (sender_id <> receiver_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE party (
    party_id    BIGINT NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    creator_id  BIGINT NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (party_id),
    KEY idx_party_creator (creator_id),

    CONSTRAINT fk_party_creator
        FOREIGN KEY (creator_id) REFERENCES member(member_id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE party_member (
    party_id    BIGINT NOT NULL,
    member_id   BIGINT NOT NULL,
    role        ENUM('OWNER', 'MEMBER') NOT NULL DEFAULT 'MEMBER',
    joined_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    owner_flag TINYINT GENERATED ALWAYS AS (CASE WHEN role = 'OWNER' THEN 1 ELSE NULL END) STORED,

    PRIMARY KEY (party_id, member_id),
    KEY idx_pm_member (member_id),
    KEY idx_pm_party_role (party_id, role),

    UNIQUE KEY uk_pm_one_owner_per_party (party_id, owner_flag),

    CONSTRAINT fk_pm_party
        FOREIGN KEY (party_id) REFERENCES party(party_id)
            ON DELETE CASCADE
            ON UPDATE CASCADE,

    CONSTRAINT fk_pm_member
        FOREIGN KEY (member_id) REFERENCES member(member_id)
            ON DELETE CASCADE
            ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE party_invite (
    invite_id       BIGINT NOT NULL AUTO_INCREMENT,
    party_id        BIGINT NOT NULL,
    inviter_id      BIGINT NOT NULL,
    invitee_id      BIGINT NOT NULL,
    status          ENUM('PENDING','ACCEPTED','REJECTED','CANCELED') NOT NULL DEFAULT 'PENDING',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    responded_at    DATETIME NULL,

    pending_flag TINYINT GENERATED ALWAYS AS (
        CASE WHEN status = 'PENDING' THEN 1 ELSE NULL END
    ) STORED,

    PRIMARY KEY (invite_id),
    KEY idx_pi_party_status_created (party_id, status, created_at),
    KEY idx_pi_invitee_status_created (invitee_id, status, created_at),

    UNIQUE KEY uk_pi_party_invitee_pending (party_id, invitee_id, pending_flag),

    CONSTRAINT fk_pi_party
        FOREIGN KEY (party_id) REFERENCES party(party_id)
            ON DELETE CASCADE
            ON UPDATE CASCADE,

    CONSTRAINT fk_pi_inviter
        FOREIGN KEY (inviter_id) REFERENCES member(member_id),
    CONSTRAINT fk_pi_invitee
        FOREIGN KEY (invitee_id) REFERENCES member(member_id),

    CONSTRAINT chk_pi_not_self CHECK (inviter_id <> invitee_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE trip (
    trip_id     BIGINT NOT NULL AUTO_INCREMENT,
    party_id    BIGINT NOT NULL,
    creator_id  BIGINT NOT NULL,
    title       VARCHAR(100) NOT NULL,
    description VARCHAR(1000) NULL,
    start_date  DATE NOT NULL,
    end_date    DATE NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (trip_id),

    CONSTRAINT fk_trip_party
        FOREIGN KEY (party_id) REFERENCES party(party_id)
            ON DELETE CASCADE
            ON UPDATE CASCADE,

    CONSTRAINT fk_trip_creator
        FOREIGN KEY (creator_id) REFERENCES member(member_id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE,

    CONSTRAINT chk_trip_date_range
        CHECK (start_date <= end_date)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE trip_day (
    trip_day_id BIGINT NOT NULL AUTO_INCREMENT,
    trip_id     BIGINT NOT NULL,
    day_num     INT NOT NULL,
    trip_date   DATE NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (trip_day_id),

    UNIQUE KEY uk_trip_day_trip_day_num (trip_id, day_num),
    UNIQUE KEY uk_trip_day_trip_date (trip_id, trip_date),

    KEY idx_trip_day_trip_id (trip_id),

    CONSTRAINT fk_trip_day_trip_id
        FOREIGN KEY (trip_id)
            REFERENCES trip (trip_id)
                ON DELETE CASCADE
                ON UPDATE CASCADE,
    CONSTRAINT chk_trip_day_num
        CHECK (day_num >= 1)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE trip_schedule (
    trip_schedule_id    BIGINT NOT NULL AUTO_INCREMENT,
    trip_day_id         BIGINT NOT NULL,
    attraction_no       INT NULL,
    title               VARCHAR(100) NOT NULL,
    memo                VARCHAR(1000) NULL,
    sort_order          INT NOT NULL,
    created_by          BIGINT NOT NULL,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (trip_schedule_id),

    KEY idx_trip_schedule_trip_day_id (trip_day_id),
    KEY idx_trip_schedule_attraction_no (attraction_no),

    UNIQUE KEY uk_trip_schedule_day_sort_order (trip_day_id, sort_order),

    CONSTRAINT fk_trip_schedule_trip_day_id
        FOREIGN KEY (trip_day_id)
            REFERENCES trip_day (trip_day_id)
            ON DELETE CASCADE
            ON UPDATE CASCADE,
    CONSTRAINT fk_trip_schedule_attraction_no
        FOREIGN KEY (attraction_no)
            REFERENCES attractions (no)
            ON DELETE SET NULL
            ON UPDATE CASCADE,
    CONSTRAINT chk_trip_schedule_sort_order
        CHECK (sort_order >= 1)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;
