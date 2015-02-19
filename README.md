
# How to use through Gradle 
Use the following in your build.gradle

```
repositories {
    mavenCentral()
    maven{
        url 'https://raw.githubusercontent.com/algobardo/jsonrpc/mvn-repo/repo'
    }
}
```
 
```
dependencies {
    compile 'dk.au.cs:jsonrpc-java:1.1'
}
```
