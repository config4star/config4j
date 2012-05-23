#!/bin/sh
cat src/org/config4j/*.java | grep -v '[ \t]*//' | grep -v '^$' | wc -l
