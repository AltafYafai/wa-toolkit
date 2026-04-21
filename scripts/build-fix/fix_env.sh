#!/usr/bin/env bash
# Source this script to fix the 'Native registration unable to find class PerfettoTrace' error
export LD_LIBRARY_PATH="/data/data/com.termux/files/home/build-fix:$LD_LIBRARY_PATH"
echo "[*] Build environment fixed. You can now run gradlew or javac."
