#!/bin/bash

# source: https://dev.kik.com/#/docs/messaging

KIK_AUTH=`cat ~/.kik-auth | base64 -`

curl -vvv -X POST \
  -H "Authorization: Basic $KIK_AUTH" \
  -H "Content-Type: application/json" \
  -d @kik-bot-config.json \
  https://api.kik.com/v1/config
