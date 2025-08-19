USE test;

INSERT INTO GATEWAY_ITEM (GATEWAY_ITEM_PK, GATEWAY_ITEM_NAME, GATEWAY_ITEM_URL, GATEWAY_ITEM_PORT, GATEWAY_ITEM_PREFIX)
VALUES (1, 'test api 1', 'http://localhost', 30001, 'api1'),
       (2, 'test api 2', 'http://localhost', 30002, null);

INSERT INTO GATEWAY_PATH (GATEWAY_PATH_PK, GATEWAY_PATH_PATH, GATEWAY_PATH_ENABLE_AUTH, GATEWAY_PATH_ROLE,
                          GATEWAY_PATH_HTTP_METHOD, GATEWAY_ITEM_PK, GATEWAY_PATH_PRIORITY, GATEWAY_PATH_IS_ENABLED)
VALUES (1, '/**', 0, null, 'GET', 1, 0, 1),
       (2, '/query/**', 0, null, 'GET', 1, 1, 1),
       (3, '/path/{id}/**', 0, null, 'GET', 1, 1, 1),
       (4, '/need-auth/**', 1, 'ROLE_ADMIN', 'GET', 1, 1, 1),
       (5, '/optional-auth/**', 0, null, 'GET', 1, 1, 1),
       (7, '/query/**', 0, null, 'GET', 2, 1, 1),
       (8, '/need-auth/**', 1, 'ROLE_ADMIN', 'GET', 2, 1, 1),
       (9, '/optional-auth/**', 0, null, 'GET', 2, 1, 0);