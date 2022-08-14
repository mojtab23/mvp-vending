curl -X POST --location "http://localhost:8080/login" \
    -b cookie.txt -c cookie.txt \
    -H "Content-Type: application/json" \
    -d "{
          \"username\": \"admin\",
          \"password\": \"admin\"
        }"
