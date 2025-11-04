-- allowing for user with no passwords
CREATE USER IF NOT EXISTS 'sa'@'%' IDENTIFIED BY '';
CREATE USER IF NOT EXISTS 'sa'@'localhost' IDENTIFIED BY '';
GRANT ALL PRIVILEGES ON `TarrificDB`.* TO 'sa'@'%';
GRANT ALL PRIVILEGES ON `TarrificDB`.* TO 'sa'@'localhost';
FLUSH PRIVILEGES;
