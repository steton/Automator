#!/bin/bash

curl --insecure -X POST -H "Authorization: OAuth 0123456789" -H "Content-Type: application/json" -H "Accept: application/json" -d "@/home/tony/Documents/DEV/eclipse-workspace/Automator/etc/exec-request.json" https://localhost:8445/servlet/tasks/exec