#!/usr/bin/env bash
cd impl
mvn license:format
cd ../examples
mvn license:format
cd ..
