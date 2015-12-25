cd src

rm -rf WEB-INF/classes
mkdir WEB-INF/classes

cp seeds WEB-INF/

javac  -classpath WEB-INF/lib/*:WEB-INF/classes -d WEB-INF/classes com/myapp/worker/utils/*.java
javac  -classpath WEB-INF/lib/*:WEB-INF/classes -d WEB-INF/classes com/myapp/worker/utils/Robots/*.java
javac  -classpath WEB-INF/lib/*:WEB-INF/classes -d WEB-INF/classes com/myapp/worker/db/*.java
javac -classpath WEB-INF/lib/*:WEB-INF/classes -d WEB-INF/classes com/myapp/worker/*.java
javac -classpath WEB-INF/lib/*:WEB-INF/classes -d WEB-INF/classes com/myapp/worker/servlet/*.java

jar -cvf worker.war WEB-INF 


mv worker.war ../

