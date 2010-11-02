
#classpath=lib/commons-cli-1.0.jar;lib/commons-codec-1.3.jar;lib/commons-collections-3.1.jar;lib/commons-httpclient-3.1.jar;lib/commons-io-1.3.1.jar;lib/commons-lang-2.3.jar;lib/commons-logging-1.0.4.jar;lib/commons-pool-1.3.jar
#classpath=%classpath%;lib/ant-1.6.2.jar;lib/dnsjava-2.0.3.jar;lib/fastutil-5.0.3-heritrix-subset-1.0.jar;lib/javaswf-CVS-SNAPSHOT-1.jar;lib/je-3.3.82.jar;lib/jetty-4.2.23.jar;lib/junit-3.8.2.jar;lib/libidn-0.5.9.jar;lib/mg4j-1.0.1.jar;lib/servlet-tomcat-4.1.30.jar
#classpath=%classpath%;lib/heritrix-1.14.4.jar

mkdir build

#javac -d build -classpath lib/heritrix-1.14.4.jar src/*.java
#javac -d build -Djava.ext.dirs=lib src/*.java

javac -d build -classpath lib/websphinx.jar src/TestCrawl4.java

java -Djava.ext.dirs=lib -classpath build TestCrawl4


if [ -d "checkpoints" ] 
then rm -r checkpoints
fi

if [ -d "logs" ] 
then rm -r logs 
fi

if [ -d "scratchrd" ] 
then rm -r scratchrd 
fi

if [ -d "state" ] 
then rm -r state 
fi

rm -r build
