CREATE TABLE users (
                       id          BIGINT       NOT NULL AUTO_INCREMENT,
                       provider    VARCHAR(20)  NOT NULL COMMENT 'KAKAO, NAVER, SYSTEM',
                       provider_id VARCHAR(100) NOT NULL,
                       nickname    VARCHAR(20)  NOT NULL,
                       email       VARCHAR(255) NULL,
                       created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       PRIMARY KEY (id),
                       UNIQUE KEY uk_users_provider (provider, provider_id),
                       UNIQUE KEY uk_users_nickname (nickname)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE playlist (
                          id                 BIGINT       NOT NULL AUTO_INCREMENT,
                          owner_id           BIGINT       NOT NULL,
                          title              VARCHAR(30)  NOT NULL,
                          description        VARCHAR(200) NULL,
                          is_public          BOOLEAN      NOT NULL DEFAULT TRUE,
                          like_count         INT          NOT NULL DEFAULT 0,
                          source_playlist_id BIGINT       NULL COMMENT '복제본일 때 원본 id',
                          created_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          deleted_at         DATETIME     NULL COMMENT 'NULL이면 삭제되지 않음',
                          PRIMARY KEY (id),
                          KEY idx_playlist_public_list (is_public, deleted_at, created_at),
                          KEY idx_playlist_owner (owner_id),
                          CONSTRAINT fk_playlist_owner  FOREIGN KEY (owner_id) REFERENCES users (id),
                          CONSTRAINT fk_playlist_source FOREIGN KEY (source_playlist_id) REFERENCES playlist (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE track (
                       id          BIGINT       NOT NULL AUTO_INCREMENT,
                       external_id BIGINT       NOT NULL COMMENT 'iTunes trackId',
                       title       VARCHAR(200) NOT NULL,
                       artist      VARCHAR(200) NOT NULL,
                       album       VARCHAR(200) NULL,
                       image_url   VARCHAR(500) NULL,
                       preview_url VARCHAR(500) NULL,
                       duration_ms INT          NULL,
                       created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       PRIMARY KEY (id),
                       UNIQUE KEY uk_track_external (external_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE playlist_track (
                                id          BIGINT      NOT NULL AUTO_INCREMENT,
                                playlist_id BIGINT      NOT NULL,
                                track_id    BIGINT      NOT NULL,
                                position    VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '정렬 키',
                                added_by    BIGINT      NOT NULL,
                                created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                PRIMARY KEY (id),
                                UNIQUE KEY uk_playlist_track (playlist_id, track_id),
                                UNIQUE KEY uk_playlist_position (playlist_id, position),
                                CONSTRAINT fk_pt_playlist FOREIGN KEY (playlist_id) REFERENCES playlist (id),
                                CONSTRAINT fk_pt_track    FOREIGN KEY (track_id) REFERENCES track (id),
                                CONSTRAINT fk_pt_added_by FOREIGN KEY (added_by) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE playlist_like (
                               id          BIGINT   NOT NULL AUTO_INCREMENT,
                               user_id     BIGINT   NOT NULL,
                               playlist_id BIGINT   NOT NULL,
                               created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               PRIMARY KEY (id),
                               UNIQUE KEY uk_like_user_playlist (user_id, playlist_id),
                               KEY idx_like_playlist (playlist_id),
                               CONSTRAINT fk_like_user     FOREIGN KEY (user_id) REFERENCES users (id),
                               CONSTRAINT fk_like_playlist FOREIGN KEY (playlist_id) REFERENCES playlist (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE collaborator (
                              id          BIGINT   NOT NULL AUTO_INCREMENT,
                              playlist_id BIGINT   NOT NULL,
                              user_id     BIGINT   NOT NULL,
                              invited_by  BIGINT   NOT NULL COMMENT '초대 링크를 만든 OWNER',
                              created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              PRIMARY KEY (id),
                              UNIQUE KEY uk_collaborator (playlist_id, user_id),
                              KEY idx_collaborator_user (user_id),
                              CONSTRAINT fk_collab_playlist   FOREIGN KEY (playlist_id) REFERENCES playlist (id),
                              CONSTRAINT fk_collab_user       FOREIGN KEY (user_id) REFERENCES users (id),
                              CONSTRAINT fk_collab_invited_by FOREIGN KEY (invited_by) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE playlist_invite (
                                 id          BIGINT      NOT NULL AUTO_INCREMENT,
                                 playlist_id BIGINT      NOT NULL,
                                 token       VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
                                 created_by  BIGINT      NOT NULL,
                                 expires_at  DATETIME    NOT NULL,
                                 created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 PRIMARY KEY (id),
                                 UNIQUE KEY uk_invite_playlist (playlist_id),
                                 UNIQUE KEY uk_invite_token (token),
                                 CONSTRAINT fk_invite_playlist   FOREIGN KEY (playlist_id) REFERENCES playlist (id),
                                 CONSTRAINT fk_invite_created_by FOREIGN KEY (created_by) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE library (
                         id         BIGINT      NOT NULL AUTO_INCREMENT,
                         user_id    BIGINT      NOT NULL,
                         name       VARCHAR(30) NOT NULL,
                         is_default BOOLEAN     NOT NULL DEFAULT FALSE,
                         created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         PRIMARY KEY (id),
                         UNIQUE KEY uk_library_user_name (user_id, name),
                         CONSTRAINT fk_library_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE library_playlist (
                                  id          BIGINT   NOT NULL AUTO_INCREMENT,
                                  library_id  BIGINT   NOT NULL,
                                  playlist_id BIGINT   NOT NULL,
                                  created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  PRIMARY KEY (id),
                                  UNIQUE KEY uk_library_playlist (library_id, playlist_id),
                                  KEY idx_lp_playlist (playlist_id),
                                  CONSTRAINT fk_lp_library  FOREIGN KEY (library_id) REFERENCES library (id),
                                  CONSTRAINT fk_lp_playlist FOREIGN KEY (playlist_id) REFERENCES playlist (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;