#!/bin/bash

# source: https://dev.kik.com/#/docs/messaging

KIK_AUTH=`cat ~/.kik-auth | base64 -`

curl -vvv -X GET \
  -H "Authorization: Basic $KIK_AUTH" \
  https://api.kik.com/v1/config
