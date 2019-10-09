docker run -d --name timeboard-mysql \
    -v ${PWD}/scripts/sql:/docker-entrypoint-initdb.d \
    -e MYSQL_ROOT_PASSWORD=timeboard \
    -e MYSQL_DATABASE=timeboard \
    -p 3306:3306 \
    mysql:8.0