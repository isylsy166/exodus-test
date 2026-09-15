package pj.exodustest.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 루트 경로 안내 페이지.
 * <p>
 * Public URL 을 브라우저로 열었을 때 제공 중인 API 목록을 바로 확인할 수 있도록 한다.
 */
@RestController
public class RootController {

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String index() {
        return """
                <!doctype html>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>짜장면 vs 짬뽕 투표 API</title>
                <style>
                  body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
                         max-width: 760px; margin: 40px auto; padding: 0 20px; line-height: 1.7;
                         color: #1a1a1a; }
                  h1 { font-size: 1.5rem; margin-bottom: 4px; }
                  .sub { color: #666; margin-top: 0; }
                  .ep { border: 1px solid #e3e3e3; border-radius: 8px; padding: 12px 16px; margin: 12px 0; }
                  .m { display: inline-block; font-size: .75rem; font-weight: 700; padding: 2px 8px;
                       border-radius: 4px; color: #fff; margin-right: 8px; }
                  .get { background: #2f7d32; }
                  .post { background: #1565c0; }
                  code { background: #f4f4f4; padding: 2px 6px; border-radius: 4px; font-size: .9rem; }
                  pre { background: #f7f7f7; padding: 12px; border-radius: 6px; overflow-x: auto;
                        font-size: .85rem; }
                  .note { color: #666; font-size: .9rem; }
                  hr { border: none; border-top: 1px solid #eee; margin: 32px 0 16px; }
                </style>

                <h1>짜장면 vs 짬뽕 투표 API</h1>
                <p class="sub">서비스 정상 동작 중 ✅</p>

                <div class="ep">
                  <span class="m post">POST</span><code>/api/vote</code>
                  <pre>{"choice": "jajang", "voterId": "user-123"}</pre>
                  <p class="note">choice 는 <code>jajang</code> 또는 <code>jjamppong</code>.
                     200 성공 / 409 중복 투표 / 400 잘못된 요청</p>
                </div>

                <div class="ep">
                  <span class="m get">GET</span><a href="/api/result"><code>/api/result</code></a>
                  <pre>{"jajang": 120, "jjamppong": 95, "total": 215}</pre>
                </div>

                <div class="ep">
                  <span class="m get">GET</span><a href="/health"><code>/health</code></a>
                  <pre>{"status": "UP", "db": "UP", "time": "..."}</pre>
                  <p class="note">DB 커넥션까지 확인한다. 정상 200 / DB 접속 불가 503</p>
                </div>

                <hr>
                <p class="note">개발 확인용 엔드포인트 —
                  <a href="/api/hello">/api/hello</a> ·
                  <a href="/api/health">/api/health</a> ·
                  <a href="/api/echo?message=ping">/api/echo</a>
                </p>
                """;
    }
}
