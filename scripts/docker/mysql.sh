docker run -d --name kronops-mysql \
    -v ${PWD}/scripts/sql:/docker-entrypoint-initdb.d \
    -e MYSQL_ROOT_PASSWORD=kronops \
    -e MYSQL_DATABASE=kronops \
    -p 3306:3306 \
    mysql:8.0