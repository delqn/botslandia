#!/bin/bash

curl  \
  -F recipient='{"id": 964035286979585}' \
  -F message='{"text": "hello"}' \
  "https://graph.facebook.com/v2.6/me/messages?access_token=`cat ~/.fb-token`"
