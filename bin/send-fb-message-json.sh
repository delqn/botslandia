#!/bin/bash

TOKEN=`cat ~/.fb-token`

USER_ID=964035286979585

curl -X POST -vvv \
     -H "Content-Type: application/json" \
     -H "Authorization: token $TOKEN" \
     -d '{
     "recipient":{ "id":"964035286979585" },
     "message":{
       "attachment":{
         "type":"template",
         "payload":{
           "template_type":"button",
           "text":"What do you want to do next?",
           "buttons":[
             {
               "type":"web_url",
               "url":"https://petersapparel.parseapp.com",
               "title":"Show Website"
             },
             {
               "type":"postback",
               "title":"Start Chatting",
               "payload":"USER_DEFINED_PAYLOAD"
             }
           ]
         }
       }
     }
   }' \
   "https://graph.facebook.com/v2.6/me/messages?access_token=$TOKEN"
