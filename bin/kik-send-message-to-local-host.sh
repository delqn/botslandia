#!/bin/bash

# source: https://dev.kik.com/#/docs/messaging

KIK_AUTH=`cat ~/.kik-auth | base64 -`

curl -vvv -X POST \
  -H "Authorization: Basic $KIK_AUTH" \
  -H "Content-Type: application/json" \
  -d @kik-sample-post.json \
  'http://localhost:9000/kik-bot'
