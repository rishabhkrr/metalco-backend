@echo off
set "JAVA_HOME=D:\MetalCO\tools\jdk-21.0.10+7"
set "MAVEN_HOME=D:\MetalCO\tools\apache-maven-3.9.14"
set "PATH=%MAVEN_HOME%\bin;%JAVA_HOME%\bin;%PATH%"
call mvn spring-boot:run
