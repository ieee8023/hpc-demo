mkdir -p classes
javac -J-Xms256m -J-Xmx256m -cp `sh getclasspath.sh` -d classes `find src -type f -name "*.java"`
