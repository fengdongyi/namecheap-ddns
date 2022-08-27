package com.nereusyi.namecheap.ddns.task;

import com.nereusyi.namecheap.ddns.config.NamecheapProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;

@Slf4j
@Component
@EnableConfigurationProperties(NamecheapProperties.class)
public class NamecheapDDnsTask {

    private static String lastIp = null;

    private final NamecheapProperties namecheapProperties;

    public NamecheapDDnsTask(NamecheapProperties namecheapProperties) {
        this.namecheapProperties = namecheapProperties;
    }

    @Scheduled(cron = "${task.cron}")
    public synchronized void execute() {
        validateProperties(namecheapProperties);
        String wanIP;
        try {
            wanIP = getWanIP();
            if (wanIP == null) {
                return;
            }
            if (Objects.equals(lastIp, wanIP)) {
                log.debug("wanIP not change,lastIP={}", lastIp);
                return;
            }
            refreshIPToNamecheap(wanIP);
            lastIp = wanIP;
        } catch (Exception e) {
            log.error("refresh wanIP failed", e);
            return;
        }
        log.info("refresh to namecheap success , new WanIP={}", wanIP);
    }

    private void validateProperties(NamecheapProperties namecheapProperties) {
        Assert.hasText(namecheapProperties.getDomain(), "domain can't be null");
        Assert.hasText(namecheapProperties.getHost(), "host can't be null");
        Assert.hasText(namecheapProperties.getPassword(), "password can't be null");
    }

    /**
     * success : <?xml version="1.0" encoding="utf-16"?><interface-response><Command>SETDNSHOST</Command><Language>eng</Language><IP>${IP}</IP><ErrCount>0</ErrCount><errors /><ResponseCount>0</ResponseCount><responses /><Done>true</Done><debug><![CDATA[]]></debug></interface-response>
     * error : <?xml version="1.0" encoding="utf-16"?><interface-response><Command>SETDNSHOST</Command><Language>eng</Language><ErrCount>1</ErrCount><errors><Err1>Passwords is empty</Err1></errors><ResponseCount>1</ResponseCount><responses><response><Description>Passwords is empty</Description><ResponseNumber>304156</ResponseNumber><ResponseString>Validation error; invalid ; password</ResponseString></response></responses><Done>true</Done><debug><![CDATA[]]></debug></interface-response>
     */
    public void refreshIPToNamecheap(String wanIP) throws IOException, InterruptedException {
        String requestUrl = String.format("https://dynamicdns.park-your-domain.com/update?host=%s&domain=%s&password=%s&ip=%s",
                namecheapProperties.getHost(), namecheapProperties.getDomain(), namecheapProperties.getPassword(), wanIP);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(requestUrl))
                .timeout(Duration.ofMinutes(2))
                .GET()
                .build();

        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String responseBody = response.body();
        log.debug("responseBody={}", responseBody);
        checkResponse(responseBody);
    }

    private void checkResponse(String responseBody) {
        int startErrorCount = responseBody.indexOf("<ErrCount>");
        if (startErrorCount == -1) {
            return;
        }
        int endErrorCount = responseBody.indexOf("</ErrCount>");
        if (endErrorCount == -1) {
            return;
        }
        String errCount = responseBody.substring(startErrorCount + "<ErrCount>".length(), endErrorCount);
        if ( ! "0".equals(errCount)) {
            log.error("refresh ip has error,responseBody={}", responseBody);
        }
    }

    public String getWanIP() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://checkip.dyndns.com/"))
                .timeout(Duration.ofMinutes(2))
                .GET()
                .build();

        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String responseBody = response.body();
        if (response.statusCode() != 200) {
            return null;
        }
        int start = responseBody.indexOf(":") + 1;
        int end = responseBody.indexOf("</", start);
        return responseBody.substring(start, end).trim();
    }
}
