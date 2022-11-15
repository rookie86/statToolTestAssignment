Сборка проекта 
mvn package
Запуск
java -jar stattool-1.0.jar test_query_2.json output_2.json

Тип операции определяется по содержимому json файла.

Конфигурация для соединения с базой данных находится в файле db.properties.
Если при запуске файл не найден, она берет настройки по умолчанию из db.properties внутри jar файла.
Настройки по умолчанию соответствуют настройкам базы данных из docker файла. 

Докер файл запускает базу данных на порту 5432 и pgAdmin на порту 5050 с пользователем postgres и паролем changeme.

Для создания базы данных с именем из db.properties без таблиц используется команда
java -jar stattool-1.0.jar --create-db   
Если такая база уже есть, то она будет удалена

Для создания таблиц используется команда. Если таблицы есть, то они будут удалены
java -jar stattool-1.0.jar --create-tables

Для создания базы данных и таблиц можно использоват одну команду
java -jar stattool-1.0.jar --create-all

В программе реализована генерация тестовых данных с помощью библиотеки Faker.
Для заполнения таблиц тестовыми данными искользуется команда
java -jar stattool-1.0.jar --generate-data
Настройки для создания тестовых данных находятся в файле dbTestData.properties
Можно выбрать количество покупателей, количество товаров, количество сделанных заказов, 
сколько товаров может находится в одном заказе, и минимальную и максимальную цену товаров.


