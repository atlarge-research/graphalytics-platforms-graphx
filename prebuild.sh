#!/bin/sh
#
# Copyright 2015 Delft University of Technology
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

granulaPatch=`pwd`/`ls -td granula*.patch`

rm -Rf /tmp/graphx
git clone https://github.com/apache/spark /tmp/graphx
cd /tmp/graphx; 

git checkout v1.1.1
git checkout -b patching
git apply -v $granulaPatch

git status

export MAVEN_OPTS="-Xmx2g -XX:MaxPermSize=512M -XX:ReservedCodeCacheSize=512m"; 
mvn clean install -Pyarn -Phadoop-2.4 -Dhadoop.version=2.4.1 -DskipTests 
