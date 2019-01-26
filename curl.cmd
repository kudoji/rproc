# list of useful curl commands

# invalid
curl --header "Content-Type: application/json" --request POST --data '{"email":"","resume":""}' http://localhost:8080/offers/1
# invalid
curl --header "Content-Type: application/json" --request POST --data '{"email":"email@email.com","resume":""}' http://localhost:8080/offers/1

curl --header "Content-Type: application/json" --request POST --data '{"email":"email1@email.com","resume":"resume text"}' http://localhost:8080/offers/1

# application status progress
curl --header "Content-Type: application/json" --request PATCH --data '{"applicationStatus":"INVITED"}}' http://localhost:8080/offers/app/1