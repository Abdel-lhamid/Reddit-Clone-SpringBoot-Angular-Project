package com.abdel.SpringRedditClone.controller;

import com.abdel.SpringRedditClone.dto.CommentsDto;
import com.abdel.SpringRedditClone.services.Impl.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/api/comments")
@AllArgsConstructor
@Tag(name = "Comments Controller", description = "")
public class CommentsController {
    private final CommentService commentService;

    @PostMapping()
    public ResponseEntity<Void> createComment(@RequestBody CommentsDto commentsDto) {
        commentService.save(commentsDto);
        return new ResponseEntity<>(CREATED);
    }

    @GetMapping(value ="/bypost",params = "postId")
    @Operation(summary = "get AllComments For Post", description = "get All Comments For Post", tags = { "Comments Controller" })
    public ResponseEntity<List<CommentsDto>> getAllCommentsForPost(@RequestParam Long postId) {
        return ResponseEntity.status(OK)
                .body(commentService.getAllCommentsForPost(postId));
    }

    @GetMapping(value ="/byuser",params = "userName")
    @Operation(summary = "get AllComments For user", description = "get All Comments For user", tags = { "Comments Controller" })
    public ResponseEntity<List<CommentsDto>> getAllCommentsForUser(@RequestParam String userName) {
        return ResponseEntity.status(OK)
                .body(commentService.getAllCommentsForUser(userName));
    }
}
