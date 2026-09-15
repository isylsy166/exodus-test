package pj.exodustest.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pj.exodustest.dto.VoteRequest;
import pj.exodustest.dto.VoteResponse;
import pj.exodustest.dto.VoteResultResponse;
import pj.exodustest.service.VoteService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;

    @PostMapping("/vote")
    public ResponseEntity<VoteResponse> vote(@RequestBody(required = false) VoteRequest request) {
        return ResponseEntity.ok(voteService.vote(request));
    }

    @GetMapping("/result")
    public ResponseEntity<VoteResultResponse> result() {
        return ResponseEntity.ok(voteService.result());
    }
}
