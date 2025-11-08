-- Create root user with password
CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED BY 'root';
CREATE USER IF NOT EXISTS 'root'@'localhost' IDENTIFIED BY 'root';
GRANT ALL PRIVILEGES ON `TarrificDB`.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON `TarrificDB`.* TO 'root'@'localhost';
FLUSH PRIVILEGES;