# list of useful curl commands

#   logging in
curl --verbose --request POST --data 'username=hr&password=hr' --cookie-jar cookie.jar http://localhost:8080/login

#   check that use are successfully logged in
curl --request GET --cookie cookie.jar http://localhost:8080/offers

#   offers backpoints

#   list all offers
curl --request GET --cookie cookie.jar http://localhost:8080/offers

#   read a single offer
curl --request GET --cookie cookie.jar http://localhost:8080/offers/1

# create an offer
curl --verbose --cookie cookie.jar --header "Content-Type: application/json" --request POST --data '{"jobTitle":"a job title","startDate":"2019-01-19"}' http://localhost:8080/offers

#   applications backpoints

#   get list of all applications
curl --request GET --cookie cookie.jar http://localhost:8080/applications

#   get list of all applications for an offer
curl --request GET --cookie cookie.jar http://localhost:8080/applications?offerId=1

#   get total number of applications
curl --cookie cookie.jar --request GET http://localhost:8080/applications/total

#   get total number of applications for an offer
curl --cookie cookie.jar --request GET http://localhost:8080/applications/total?offerId=1

#   read a single application
curl --cookie cookie.jar --request GET http://localhost:8080/applications/1

#   create an application
#   invalid
curl --cookie cookie.jar --header "Content-Type: application/json" --request POST --data '{"email":"","resume":""}' http://localhost:8080/applications
#   invalid
curl --cookie cookie.jar --header "Content-Type: application/json" --request POST --data '{"email":"email@email.com","resume":""}' http://localhost:8080/applications
#   valid
curl --cookie cookie.jar --header "Content-Type: application/json" --request POST --data '{"email":"email1@email.com","resume":"resume text", "offerId": 1}' http://localhost:8080/applications

#   progress application's status
curl --cookie cookie.jar --header "Content-Type: application/json" --request PATCH --data '{"applicationStatus":"INVITED"}' http://localhost:8080/applications/1/status