mkdir -p classes
javac -cp `sh getclasspath.sh` -d classes `find src -type f -name "*.java"`
