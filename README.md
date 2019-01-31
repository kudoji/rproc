## rproc

**rproc** is a backend service that handles a very simple recruiting process.

The service communicates json over http (REST) with proper status codes returned for the most common problems.

The service is built on Java and uses the following auxiliary technologies:

* Spring Boot 2.1.2;
* Hibernate + H2 database
* RabbitMQ
* lombok 1.18.4

___
<a name="toc"></a>
# Table of contents

1. [**building**](#building)
1. [**usage**](#usage)
    1. [working with offers](#offer-create)
        1. [creating a job offer](#offer-create)
        1. [reading a single offer](#offer-read)
        1. [list all offers](#offer-list)
    1. [working with applications](#app-create)
        1. [creating an application](#app-create)
        1. [reading a single application](#app-read)
        1. [list all applications](#app-list)
        1. [list all applications for an offer](#app-offer-list)
        1. [get total number of applications](#app-total)
        1. [get total number of applications for an offer](#app-offer-total)
    1. [application statuses](#app-status)
        1. [progress the status of an application](#app-status-progress)
1. [**security**](#security)
1. [**known issues**](#issues)
1. [**RabbitMQ server installation**](#rabbitmq)


___
<a name="building"></a>
## building
[back](#toc)

**rproc** uses maven build tool which makes building process easy as follows:

    mvn clean package

The command above must be executed in the projects root.
To run the service use:

    java -jar ./target/rproc-[VERSION].jar

where \[VERSION] is a currently built version.

<a name="usage"></a>
## usage
[back](#toc)

By default the service listens to http://localhost:8080 url, so all examples are relative to the following url.

<a name="offer-create"></a>
### creating a job offer
[back](#toc)


    POST /offers

the following json structure is acceptable:

    {
        "jobTitle": "a job title",
        "startDate": "2019-01-19"
    }

Successful offer creation returns '201 Created' HTTP status with created offer details, e.g.:

    201 Created

    {
        "id": 1,
        "jobTitle": "a job title",
        "startDate": "2019-01-19",
        "numberOfApplications": 0,
        "_links":{
            "self":{
                "href": "[host]/offers/[offerId]"
            }
        }
    }

where \[offerId] is created offer id.

In case of error, e.g.:

    400  Bad Request

    {
        "errorMessage": "Validation failed due to 1 errors found",
        "errors": ["Job start date cannot be in the past"]
    }

where "**errorMessage**" is error description and "**errors**" is an optional array with the list of errors occurred.

<a name="offer-read"></a>
### reading a single offer
[back](#toc)


    GET /offers/[id]

where **\[id]** is an offer's id number.

Successful request returns '200 OK' with offer's body as follows:

    200 OK

    {
        "id": 1,
        "jobTitle": "a job title",
        "startDate": "2019-01-19",
        "numberOfApplications": 0,
        "_links":{
            "self":{
                "href": "[host]/offers/1"
            }
        }
    }

In case of error, assume that offer with id = 12 does not exists:

    GET /offers/12

returns:

    404  Not Found

    {
        "errorMessage": "Error: offer with #12 not found"
    }

<a name="offer-list"></a>
### list all offers
[back](#toc)


    GET /offers

returns array of all available offers:

    200 OK

    {
        "_embedded": {
            "offerList":
            [
                {
                    "id": 1,
                    "jobTitle":"a job title",
                    "startDate": "2019-01-19",
                    "numberOfApplications": 4,
                    "_links": {
                        "self": {
                            "href": "[host]/offers/1"
                        }
                    }
                },
                {
                    "id": 25,
                    "jobTitle": "another job title",
                    "startDate": "2019-02-19",
                    "numberOfApplications":0,
                    "_links": {
                        "self": {
                            "href": "[host]/offers/25"
                        }
                    }
                }
            ]
        },
        "_links": {
            "self": {
                "href": "http://localhost:8080/offers"
            }
        }
    }

or structure like this with no offers available:

    200 OK

    {
        "_links": {
            "self": {
                "href": "http://localhost:8080/offers"
            }
        }
    }


<a name="app-create"></a>
### creating an application
[back](#toc)

Candidate is able to create an application for particular offer.

    POST /applications

the following structure:

    {
        "email": "email1@email.com",
        "resume": "resume text",
        "offerId": [offerId]
    }

where \[**offerId**] is the offer id application for is going to be created.

Possible responses are:

    201 Created

    {
        "email": "email1@email.com",
        "resume": "resume text",
        "offerId": [offerId]
    }

in case of successful application creation;

    400  Bad Request

    {
        "errorMessage": "candidate is already submitted resume for the offer"
    }

if candidate is already submitted resume to the offer;

    404  Not Found

    {
        "errorMessage": "Error: offer with #112 not found"
    }

if offer does not exist;

    400  Bad Request

    {
        "errorMessage": "Validation failed due to 2 errors found",
        "errors":
        [
            "Candidate email cannot be empty",
            "Resume cannot be empty"
        ]
    }

in case of validation error(s).

<a name="app-read"></a>
### reading a single application
[back](#toc)

User is able to read a single application for an offer.

    GET /applications/[appId]

where \[appId] requested application Id

E.g.

    GET /applications/1

might return:

    200 OK

    {
        "id": 1,
        "email": "email@email.com",
        "resume":"resume1",
        "applicationStatus": "INVITED",
        "_links":{
            "self":{
                "href": "[host]/applications/1"
            }
        }
    }


<a name="app-list"></a>
### list all applications
[back](#toc)

User is able to get list of all applications.

    GET /applications

Possible return:

    200 OK

    {
        "_embedded":{
            "applicationList":
            [
                {
                    "id": 1,
                    "email": "email@email.com",
                    "resume": "resume1",
                    "applicationStatus":"INVITED",
                    "_links":{
                        "self":{
                            "href": "[host]/applications/1"
                        }
                    }
                },
                {
                    "id": 3,
                    "email": "email3@email.com",
                    "resume": "text",
                    "applicationStatus":"APPLIED"
                    "_links":{
                        "self":{
                            "href": "[host]/applications/3"
                        }
                    }
                },
                {
                    "id": 9,
                    "email": "email9@email.com",
                    "resume": "text",
                    "applicationStatus":"APPLIED",
                    "_links":{
                        "self":{
                            "href": "[host]/applications/9"
                        }
                    }
                }
            ],
        },
        "_links":{
            "self":{
                "href": "[host]/applications"
            }
        }
    }


<a name="app-offer-list"></a>
### list all applications for an offer
[back](#toc)

User is able to get list of all applications for an offer.

    GET /applications?offerId=[offerId]

where \[offerId] requested offer user want to get applications for.

E.g.

    GET /applications?offerId=1

might return:

    200 OK

    {
        "_embedded":{
            "applicationList":
            [
                {
                    "id": 1,
                    "email": "email@email.com",
                    "resume": "resume1",
                    "applicationStatus":"INVITED",
                    "_links":{
                        "self":{
                            "href": "[host]/applications/1"
                        }
                    }
                },
                {
                    "id": 9,
                    "email": "email9@email.com",
                    "resume": "text",
                    "applicationStatus":"APPLIED",
                    "_links":{
                        "self":{
                            "href": "[host]/applications/9"
                        }
                    }
                }
            ],
        },
        "_links":{
            "self":{
                "href": "[host]/applications?offerId=1"
            }
        }
    }


<a name="app-total"></a>
### get total number of applications
[back](#toc)

User is able to get total number of applications

    GET /applications/total

Possible response:

    200 OK

    {
        "total": 5
    }


<a name="app-offer-total"></a>
### get total number of applications for an offer
[back](#toc)

User is able to get total number of applications for a particular offer.

    GET /applications/total?offerId=[offerId]

where \[**offerId**] is offer id number applications is requested for.

Possible response:

    200 OK

    {
        "total": 3
    }


<a name="app-status"></a>
### application statuses
[back](#toc)

Each candidate's applications can have there statuses:

* APPLIED
* INVITED
* REJECTED
* HIRED

When application is just create is gets status 'APPLIED'.

Application status can be changed over time. Possible scenarios of status' progress are:

* APPLIED --> INVITED;
* APPLIED --> REJECTED;
* INVITED --> REJECTED;
* INVITED --> HIRED.

Other scenarios are treated as error with response '**400 Bad Request**'.


<a name="app-status-progress"></a>
### progress the status of an application
[back](#toc)

User is able to progress the application's status

    PATCH /applications/[appId]/status

where \[**appId**] is application id which status is need to be progressed

the following json structure is acceptable:

    {
        "applicationStatus": "INVITED"
    }

"**applicationStatus**" can take one of the listed above statues.

Possible responses:

    200 OK

    {
        "applicationStatus": "INVITED",
        "status": "updated"
    }

if status updated successfully;

    500  Internal Server Error

    {
        "errorMessage": "Application status is incorrect"
    }

if status is invalid or out of the listed above scenarios;

    404  Not Found

    {
        "errorMessage": "Error: application with #[appId] not found"
    }

if application with the id \[**appId**] does not exist.

    Each successful status change triggers a notification.
    Each notification is put into rabbitmq queue with routing-key "heavenhr.rproc.application.queue"

    Note: to make this functionality work correctly you need RabbitMQ server installed.


<a name="security"></a>
### security
[back](#toc)

All http requests except progressing the status of an applications (see [progress the status of an application](#app-status-progress) section) require authentication.
Default credentials are:

    username: hr
    password: hr

Consult curl.cmd file to check how to log in with curl

<a name="issues"></a>
### known issues
[back](#toc)

These are issues currencly known that scheduled to be fixed in upcoming release:

- current implementation stores resume text in db directly which is not a good approach;
- **POST /applications** response doesn't contain link to the created resource;
- **PATCH /applications/\[appId]/status** response doesn't contain link to the patched resource;

<a name="rabbitmq"></a>
### RabbitMQ server installation
[back](#toc)

The easiest way installing RabbitMQ is to use docker image.

    docker run --rm --publish 5672:5672 --name rabbit rabbitmq:alpine

After running the docker image, configure **spring.rabbitmq.host** in **application.properties** file.