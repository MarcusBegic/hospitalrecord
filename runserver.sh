#!/bin/zsh
st -e 'touch marcus'

javac server.java
java server 9876
