package com.backend.stageconnect.controller;

import com.backend.stageconnect.dto.CommentDTO;
import com.backend.stageconnect.entity.Comment;
import com.backend.stageconnect.entity.Post;
import com.backend.stageconnect.entity.User;
import com.backend.stageconnect.repository.CommentRepository;
import com.backend.stageconnect.repository.PostRepository;
import com.backend.stageconnect.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(
    origins = {"http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5173", "http://127.0.0.1:3000"},
    allowCredentials = "true",
    allowedHeaders = {"Origin", "Content-Type", "Accept", "Authorization", "X-Requested-With", "Access-Control-Request-Method", "Access-Control-Request-Headers", "x-cors-debug"},
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}
)
public class CommentController {

    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // Get comments for a post (paginated, top-level only)
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<Map<String, Object>> getPostComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            // Check if post exists
            if (!postRepository.existsById(postId)) {
                return ResponseEntity.notFound().build();
            }
            
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Comment> commentsPage = commentRepository.findByPostIdAndParentCommentIsNullOrderByCreatedAtDesc(postId, pageable);
            
            List<CommentDTO> comments = commentsPage.getContent().stream()
                    .map(comment -> CommentDTO.fromEntity(comment, true)) // Include replies
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("comments", comments);
            response.put("currentPage", commentsPage.getNumber());
            response.put("totalItems", commentsPage.getTotalElements());
            response.put("totalPages", commentsPage.getTotalPages());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to fetch comments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // Get replies for a comment
    @GetMapping("/comments/{commentId}/replies")
    public ResponseEntity<Map<String, Object>> getCommentReplies(@PathVariable Long commentId) {
        try {
            // Check if comment exists
            if (!commentRepository.existsById(commentId)) {
                return ResponseEntity.notFound().build();
            }
            
            List<Comment> replies = commentRepository.findByParentCommentIdOrderByCreatedAtAsc(commentId);
            
            List<CommentDTO> replyDtos = replies.stream()
                    .map(reply -> CommentDTO.fromEntity(reply, false)) // Don't include nested replies
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("replies", replyDtos);
            response.put("count", replyDtos.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to fetch replies: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // Create a comment on a post
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<?> createComment(
            @PathVariable Long postId,
            @RequestBody Map<String, Object> requestBody) {
        
        try {
            // Extract values from request body
            Long userId = Long.valueOf(requestBody.get("user_id").toString());
            String content = (String) requestBody.get("content");
            
            // Validate input
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Comment content is required"
                ));
            }
            
            // Check if post exists
            Post post = postRepository.findById(postId).orElse(null);
            if (post == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Post not found with ID: " + postId
                ));
            }
            
            // Check if user exists
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found with ID: " + userId
                ));
            }
            
            // Create the comment
            Comment comment = new Comment();
            comment.setContent(content);
            comment.setAuthor(user);
            comment.setPost(post);
            comment.setLikeCount(0);
            
            Comment savedComment = commentRepository.save(comment);
            
            // Update post comment count
            post.setCommentCount(post.getCommentCount() + 1);
            postRepository.save(post);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Comment created successfully",
                "data", CommentDTO.fromEntity(savedComment, false)
            ));
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to create comment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // Create a reply to a comment
    @PostMapping("/comments/{commentId}/replies")
    public ResponseEntity<?> createReply(
            @PathVariable Long commentId,
            @RequestBody Map<String, Object> requestBody) {
        
        try {
            // Extract values from request body
            Long userId = Long.valueOf(requestBody.get("user_id").toString());
            String content = (String) requestBody.get("content");
            
            // Validate input
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Reply content is required"
                ));
            }
            
            // Check if parent comment exists
            Comment parentComment = commentRepository.findById(commentId).orElse(null);
            if (parentComment == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Parent comment not found with ID: " + commentId
                ));
            }
            
            // Check if user exists
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found with ID: " + userId
                ));
            }
            
            // Create the reply
            Comment reply = new Comment();
            reply.setContent(content);
            reply.setAuthor(user);
            reply.setParentComment(parentComment);
            reply.setPost(parentComment.getPost()); // Set the same post as parent comment
            reply.setLikeCount(0);
            
            Comment savedReply = commentRepository.save(reply);
            
            // Update post comment count
            Post post = parentComment.getPost();
            if (post != null) {
                post.setCommentCount(post.getCommentCount() + 1);
                postRepository.save(post);
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Reply created successfully",
                "data", CommentDTO.fromEntity(savedReply, false)
            ));
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to create reply: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // Update a comment
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable Long commentId,
            @RequestBody Map<String, Object> requestBody) {
        
        try {
            // Extract values from request body
            String content = (String) requestBody.get("content");
            
            // Validate input
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Comment content is required"
                ));
            }
            
            return commentRepository.findById(commentId)
                    .map(comment -> {
                        comment.setContent(content);
                        Comment updatedComment = commentRepository.save(comment);
                        
                        return ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "Comment updated successfully",
                            "data", CommentDTO.fromEntity(updatedComment, false)
                        ));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update comment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // Delete a comment
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {
        try {
            return commentRepository.findById(commentId)
                    .map(comment -> {
                        // Update post comment count
                        Post post = comment.getPost();
                        if (post != null) {
                            // Count this comment and all its replies
                            int totalComments = 1 + (comment.getReplies() != null ? comment.getReplies().size() : 0);
                            post.setCommentCount(Math.max(0, post.getCommentCount() - totalComments));
                            postRepository.save(post);
                        }
                        
                        commentRepository.delete(comment);
                        
                        return ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "Comment deleted successfully"
                        ));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to delete comment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // Like a comment
    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<?> likeComment(
            @PathVariable Long commentId,
            @RequestBody Map<String, Object> requestBody) {
        
        try {
            // Extract values from request body
            Long userId = Long.valueOf(requestBody.get("user_id").toString());
            
            // Check if user exists
            if (!userRepository.existsById(userId)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found with ID: " + userId
                ));
            }
            
            return commentRepository.findById(commentId)
                    .map(comment -> {
                        // Increment like count
                        comment.setLikeCount(comment.getLikeCount() + 1);
                        Comment updatedComment = commentRepository.save(comment);
                        
                        return ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "Comment liked successfully",
                            "likeCount", updatedComment.getLikeCount()
                        ));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to like comment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // Unlike a comment
    @PostMapping("/comments/{commentId}/unlike")
    public ResponseEntity<?> unlikeComment(
            @PathVariable Long commentId,
            @RequestBody Map<String, Object> requestBody) {
        
        try {
            // Extract values from request body
            Long userId = Long.valueOf(requestBody.get("user_id").toString());
            
            // Check if user exists
            if (!userRepository.existsById(userId)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found with ID: " + userId
                ));
            }
            
            return commentRepository.findById(commentId)
                    .map(comment -> {
                        // Decrement like count, but ensure it doesn't go below 0
                        comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
                        Comment updatedComment = commentRepository.save(comment);
                        
                        return ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "Comment unliked successfully",
                            "likeCount", updatedComment.getLikeCount()
                        ));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to unlike comment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
} 