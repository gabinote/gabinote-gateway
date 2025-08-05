
CREATE TABLE GATEWAY_ITEM (
                              GATEWAY_ITEM_PK BIGINT AUTO_INCREMENT PRIMARY KEY,
                              GATEWAY_ITEM_NAME VARCHAR(255) NOT NULL,
                              GATEWAY_ITEM_PREFIX VARCHAR(255) NOT NULL,
                              GATEWAY_ITEM_URL VARCHAR(2048) NOT NULL,
                              GATEWAY_ITEM_PORT INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE GATEWAY_PATH (
                              GATEWAY_PATH_PK BIGINT AUTO_INCREMENT PRIMARY KEY,
                              GATEWAY_PATH_PATH VARCHAR(255) NOT NULL,
                              GATEWAY_PATH_ENABLE_AUTH BOOLEAN NOT NULL,
                              GATEWAY_PATH_ROLE VARCHAR(255) NOT NULL,
                              GATEWAY_PATH_HTTP_METHOD
                                  ENUM('GET','POST','PUT','PATCH','DELETE','HEAD','OPTIONS','TRACE','CONNECT')
        NOT NULL,
                              GATEWAY_ITEM_PK BIGINT NOT NULL,
                              GATEWAY_PATH_PRIORITY INT NOT NULL,
                              CONSTRAINT fk_gateway_path_item
                                  FOREIGN KEY (GATEWAY_ITEM_PK)
                                      REFERENCES GATEWAY_ITEM (GATEWAY_ITEM_PK)
                                      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

