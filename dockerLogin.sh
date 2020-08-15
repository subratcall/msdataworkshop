#!/bin/bash

if [[ $1 == "" ]]
then
  echo Required argument USERNAME
  echo Usage example : ./dockerLogin.sh idg2lobfjar3/my@email.com
  exit
fi

#eg docker login us-ashburn-1.ocir.io -u $1