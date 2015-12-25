cd src
rm -rf WEB-INF/classes
mkdir WEB-INF/classes
javac  -classpath WEB-INF/lib/*:WEB-INF/classes -d WEB-INF/classes com/myapp/master/*.java

jar -cvf master.war seeds WEB-INF 
mv master.war ../


