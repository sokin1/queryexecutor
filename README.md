# sqlexecutor

How to start the sqlexecutor application
---

1. Run `mvn clean install` to build your application
1. Start application with `java -jar target/apiserver-1.0-SNAPSHOT.jar server config.yml`
1. To check that your application is running enter url `http://localhost:8080`

Health Check
---

To see your applications health enter url `http://localhost:8081/healthcheck`


Database Setup
---

MariaDB : 
1. docker run -p 127.0.0.1:3306:3306 --detach --name grepr-mariadb --env MARIADB_USER=jaekuk --env MARIADB_PASSWORD=grepr --env MARIADB_DATABASE=test-database --env MARIADB_ROOT_PASSWORD=grepr mariadb:latest

2. docker exec -it grepr-mariadb mariadb --user jaekuk -pgrepr

3. MariaDB [test-database]> desc products_tbl
    -> ;
+----------------------+--------------+------+-----+---------+----------------+
| Field                | Type         | Null | Key | Default | Extra          |
+----------------------+--------------+------+-----+---------+----------------+
| product_id           | int(11)      | NO   | PRI | NULL    | auto_increment |
| product_name         | varchar(100) | NO   |     | NULL    |                |
| product_manufacturer | varchar(40)  | NO   |     | NULL    |                |
| submission_date      | date         | YES  |     | NULL    |                |
+----------------------+--------------+------+-----+---------+----------------+
4 rows in set (0.004 sec)



Key-value store Setup
---

Redis:
1. docker run -d --name test-redis -p 6379:6379 redis/redis-stack-server:latest
2. docker exec -it test-redis redis-cli

Instruction of Execution
---

0. Create table as described above
1. run `/sqlquery/insert?num=[number of record]` to generate records to the table
2. run `/sqlquery/requestHandle` to get new handle
3. run `/sqlquery/select?handle=[handle]&size=[size of results]` to get query results