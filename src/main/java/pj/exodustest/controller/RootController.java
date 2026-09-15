package pj.exodustest.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 브라우저로 터널 주소를 열었을 때 바로 보이는 안내 페이지.
 */
@RestController
public class RootController {

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String index() {
        return """
                <!doctype html>
                <meta charset="utf-8">
                <title>exodusTest</title>
                <h1>exodusTest is running ✅</h1>
                <ul>
                  <li><a href="/health">GET /health</a></li>
                  <li><a href="/api/result">GET /api/result</a></li>
                  <li>POST /api/vote (JSON body: {"choice":"jajang|jjamppong","voterId":"..."})</li>
                </ul>
                <hr>
                <ul>
                  <li><a href="/api/hello">GET /api/hello</a></li>
                  <li><a href="/api/health">GET /api/health</a></li>
                  <li><a href="/api/echo?message=ngrok">GET /api/echo?message=ngrok</a></li>
                  <li>POST /api/echo (JSON body)</li>
                </ul>
                """;
    }
}
