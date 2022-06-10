COPY MENU
FROM '/Users/owendolan/Workspace/CS166-FinalProject/data/menu.csv'
WITH DELIMITER ';';

COPY USERS
FROM '/Users/owendolan/Workspace/CS166-FinalProject/data/users.csv'
WITH DELIMITER ';';

COPY ORDERS
FROM '/Users/owendolan/Workspace/CS166-FinalProject/data/orders.csv'
WITH DELIMITER ';';
ALTER SEQUENCE orders_orderid_seq RESTART 87257;

COPY ITEMSTATUS
FROM '/users/owendolan/Workspace/CS166-FinalProject/data/itemStatus.csv'
WITH DELIMITER ';';

