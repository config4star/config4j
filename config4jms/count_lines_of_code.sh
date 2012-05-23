#!/bin/sh
cat src/org/config4jms/*.java src/org/config4jms/*/*.java  | grep -v '[ \t]*//' | grep -v '^$' | wc -l
