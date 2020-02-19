#!/bin/bash
echo 'Starting my app'
# shellcheck disable=SC2164
cd '/home/ec2-user'
nohup sh build/install/revolut-interview/bin/revolut-interview > /dev/null 2>&1 &
