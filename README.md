# Namecheap DDNS

Simple Namecheap DDNS Refresh Schedule

# Maven Run
```java
 mvn spring-boot:run -Dnamecheap.host=${yourHost} -Dnamecheap.domain=${yourDomain} -Dnamecheap.password=${yourPassword} -Dtask.cron=0 0/10 * * * ?
```