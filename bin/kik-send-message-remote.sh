#!/bin/bash

# source: https://dev.kik.com/#/docs/messaging

KIK_AUTH=`cat ~/.kik-auth | base64 -`

curl -vvv -X POST \
  -H "Authorization: Basic $KIK_AUTH" \
  -H "Content-Type: application/json" \
  -d '{
          "messages": [
              {
                  "chatId": "0c4701eba03e11cec0a13ecbd8e96093718f2db18b5e85e9c29ecdcaa0f32dcd",
                  "type": "is-typing",
                  "to": "delyan",
                  "isTyping": true
              }
          ]
      }' \
  https://api.kik.com/v1/message

sleep 1

curl -vvv -X POST \
  -H "Authorization: Basic $KIK_AUTH" \
  -H "Content-Type: application/json" \
  -d "{\"messages\":[
          {
              \"body\": \"Hi there how are you\",
              \"keyboards\": [
                {
                    \"type\": \"suggested\",
                    \"responses\": [
                        {
                            \"type\": \"text\",
                            \"body\": \"Good :)\"
                        },
                        {
                            \"type\": \"text\",
                            \"body\": \"Not so good :(\"
                        }
                    ]
              }],
              \"to\": \"delyan\",
              \"type\": \"text\",
              \"chatId\": \"0c4701eba03e11cec0a13ecbd8e96093718f2db18b5e85e9c29ecdcaa0f32dcd\"
          }
      ]
  }" \
  https://api.kik.com/v1/message

sleep 1

curl -vvv -X POST \
  -H "Authorization: Basic $KIK_AUTH" \
  -H "Content-Type: application/json" \
  -d '{
          "messages": [
              {
                  "chatId": "0c4701eba03e11cec0a13ecbd8e96093718f2db18b5e85e9c29ecdcaa0f32dcd",
                  "type": "read-receipt",
                  "to": "delyan",
                  "id": "6d8d060c-3ae4-46fc-bb18-6e7ba3182c0f",
                  "messageIds": ["3f94feaf-704f-4fbf-9bb4-3fd17151fb59", "31651aa2-454f-45f3-a33b-4dd37c7165b2"]
              }
          ]
      }' \
  https://api.kik.com/v1/message
