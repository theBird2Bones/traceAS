 #Задача
Трассировка автономных систем. Пользователь вводит доменное имя
или IP адрес. Осуществляется трассировка до указанного узла (например, с использованием
tracert), т. е. мы узнаем IP адреса маршрутизаторов, через которые проходит пакет. Необходимо определить к какой автономной системе относится каждый из полученных IP адресов
маршрутизаторов
## Использование
+ java /path/to/script [name or ip]

###Примеры
+ java ./traceAS.java vk.com 
+ java ./traceAS.java 174.138.106.148

###Requirements
+ java 9+