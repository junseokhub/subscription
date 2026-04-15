INSERT IGNORE INTO channels (name, type, created_at, updated_at) VALUES
('홈페이지',  'BOTH',        NOW(), NOW()),
('모바일앱',  'BOTH',        NOW(), NOW()),
('네이버',    'SUBSCRIBE',   NOW(), NOW()),
('SKT',       'SUBSCRIBE',   NOW(), NOW()),
('콜센터',    'UNSUBSCRIBE', NOW(), NOW()),
('이메일',    'UNSUBSCRIBE', NOW(), NOW());
