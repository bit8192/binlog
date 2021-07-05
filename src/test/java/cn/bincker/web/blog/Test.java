package cn.bincker.web.blog;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Test {
    @org.junit.jupiter.api.Test
    public void test() throws InterruptedException, IOException {
        HttpClient client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder().GET().uri(URI.create("https://www.baidu.com")).build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
    }
}
