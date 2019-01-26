# list of useful curl commands

# create an offer
curl --header "Content-Type: application/json" --request POST --data '{"jobTitle":"a job title","startDate":"2019-01-19"}' http://localhost:8080/offers/
curl --header "Content-Type: application/json" --request GET http://localhost:8080/offers/1

# create an application
# invalid
curl --header "Content-Type: application/json" --request POST --data '{"email":"","resume":""}' http://localhost:8080/offers/1
# invalid
curl --header "Content-Type: application/json" --request POST --data '{"email":"email@email.com","resume":""}' http://localhost:8080/offers/1
# valid
curl --header "Content-Type: application/json" --request POST --data '{"email":"email1@email.com","resume":"resume text"}' http://localhost:8080/offers/1

# read a single application
curl --header "Content-Type: application/json" --request GET http://localhost:8080/offers/1/1
# get all applications
curl --header "Content-Type: application/json" --request GET http://localhost:8080/offers/1/all

# get total number of applications
curl --header "Content-Type: application/json" --request GET http://localhost:8080/offers/apps_total

# get total number of applications for offer 1
curl --header "Content-Type: application/json" --request GET http://localhost:8080/offers/1/apps_total

# application status progress
curl --header "Content-Type: application/json" --request PATCH --data '{"applicationStatus":"INVITED"}' http://localhost:8080/offers/app/1