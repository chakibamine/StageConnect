package com.backend.stageconnect.controller;

import com.backend.stageconnect.dto.PostDTO;
import com.backend.stageconnect.entity.Post;
import com.backend.stageconnect.entity.User;
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
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(
    origins = {"http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5173", "http://127.0.0.1:3000"},
    allowCredentials = "true",
    allowedHeaders = {"Origin", "Content-Type", "Accept", "Authorization", "X-Requested-With", "Access-Control-Request-Method", "Access-Control-Request-Headers", "x-cors-debug"},
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}
)
public class PostController {

    private static final String UPLOAD_DIR = "./uploads/posts/";
    
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;
    
    // Get the current user's feed (their posts and posts from users they follow)
    @GetMapping("/feed")
    public ResponseEntity<Map<String, Object>> getFeed(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            User currentUser = (User) authentication.getPrincipal();
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            
            Page<Post> postsPage = postRepository.findFeedPostsForUser(currentUser.getId(), pageable);
            
            List<PostDTO> posts = postsPage.getContent().stream()
                    .map(PostDTO::fromEntity)
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("posts", posts);
            response.put("currentPage", postsPage.getNumber());
            response.put("totalItems", postsPage.getTotalElements());
            response.put("totalPages", postsPage.getTotalPages());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to fetch feed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // Get all posts (for admin or public timeline)
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Post> postsPage = postRepository.findAllByOrderByCreatedAtDesc(pageable);
            
            List<PostDTO> posts = postsPage.getContent().stream()
                    .map(PostDTO::fromEntity)
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("posts", posts);
            response.put("currentPage", postsPage.getNumber());
            response.put("totalItems", postsPage.getTotalElements());
            response.put("totalPages", postsPage.getTotalPages());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to fetch posts: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // Get posts by a specific user
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Post> postsPage = postRepository.findByAuthorId(userId, pageable);
            
            List<PostDTO> posts = postsPage.getContent().stream()
                    .map(PostDTO::fromEntity)
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("posts", posts);
            response.put("currentPage", postsPage.getNumber());
            response.put("totalItems", postsPage.getTotalElements());
            response.put("totalPages", postsPage.getTotalPages());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to fetch user posts: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // Get a specific post
    @GetMapping("/{postId}")
    public ResponseEntity<?> getPost(@PathVariable Long postId) {
        try {
            return postRepository.findById(postId)
                    .map(post -> ResponseEntity.ok(PostDTO.fromEntity(post)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to fetch post: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // Create a new post
    @PostMapping
    public ResponseEntity<?> createPost(
            @RequestParam("user_id") Long userId,
            @RequestParam("content") String content,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found with ID: " + userId
                ));
            }
            
            // Create post
            Post post = new Post();
            post.setContent(content);
            post.setAuthor(user);
            post.setLikeCount(0);
            post.setCommentCount(0);
            
            // Process image if provided
            if (image != null && !image.isEmpty()) {
                // Create upload directory if it doesn't exist
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }
                
                // Generate unique filename for the image
                String originalFilename = image.getOriginalFilename();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String filename = UUID.randomUUID().toString() + fileExtension;
                
                // Save the file
                Path filePath = Paths.get(UPLOAD_DIR + filename);
                Files.write(filePath, image.getBytes());
                
                // Create URL for accessing the image
                String imageUrl = "/uploads/posts/" + filename;
                post.setImageUrl(imageUrl);
            }
            
            Post savedPost = postRepository.save(post);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Post created successfully",
                "data", PostDTO.fromEntity(savedPost)
            ));
        } catch (IOException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to create post: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // Update a post
    @PutMapping("/{postId}")
    public ResponseEntity<?> updatePost(
            @PathVariable Long postId,
            @RequestParam("content") String content,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        
        try {
            return postRepository.findById(postId)
                    .map(post -> {
                        post.setContent(content);
                        
                        // Process image if provided
                        if (image != null && !image.isEmpty()) {
                            try {
                                // Delete old image if exists
                                if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
                                    String oldFilename = post.getImageUrl().substring(post.getImageUrl().lastIndexOf("/") + 1);
                                    Path oldFilePath = Paths.get(UPLOAD_DIR + oldFilename);
                                    Files.deleteIfExists(oldFilePath);
                                }
                                
                                // Create upload directory if it doesn't exist
                                File uploadDir = new File(UPLOAD_DIR);
                                if (!uploadDir.exists()) {
                                    uploadDir.mkdirs();
                                }
                                
                                // Generate unique filename for the image
                                String originalFilename = image.getOriginalFilename();
                                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                                String filename = UUID.randomUUID().toString() + fileExtension;
                                
                                // Save the file
                                Path filePath = Paths.get(UPLOAD_DIR + filename);
                                Files.write(filePath, image.getBytes());
                                
                                // Update image URL
                                post.setImageUrl("/uploads/posts/" + filename);
                            } catch (IOException e) {
                                throw new RuntimeException("Failed to upload image: " + e.getMessage());
                            }
                        }
                        
                        Post updatedPost = postRepository.save(post);
                        
                        return ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "Post updated successfully",
                            "data", PostDTO.fromEntity(updatedPost)
                        ));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update post: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // Update a post with image
    @PutMapping("/{postId}/with-image")
    public ResponseEntity<?> updatePostWithImage(
            Authentication authentication,
            @PathVariable Long postId,
            @RequestPart("content") String content,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        
        try {
            User currentUser = (User) authentication.getPrincipal();
            
            return postRepository.findById(postId)
                    .map(post -> {
                        // Check if the current user is the author of the post
                        if (!post.getAuthor().getId().equals(currentUser.getId())) {
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                                "success", false,
                                "message", "You are not authorized to update this post"
                            ));
                        }
                        
                        post.setContent(content);
                        
                        // Handle image update if provided
                        if (image != null && !image.isEmpty()) {
                            try {
                                // Create upload directory if it doesn't exist
                                File uploadDir = new File(UPLOAD_DIR);
                                if (!uploadDir.exists()) {
                                    uploadDir.mkdirs();
                                }
                                
                                // Delete old image if exists
                                if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
                                    String oldFilename = post.getImageUrl().substring(post.getImageUrl().lastIndexOf("/") + 1);
                                    Path oldFilePath = Paths.get(UPLOAD_DIR + oldFilename);
                                    Files.deleteIfExists(oldFilePath);
                                }
                                
                                // Generate unique filename for the image
                                String originalFilename = image.getOriginalFilename();
                                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                                String filename = UUID.randomUUID().toString() + fileExtension;
                                
                                // Save the file
                                Path filePath = Paths.get(UPLOAD_DIR + filename);
                                Files.write(filePath, image.getBytes());
                                
                                // Update image URL
                                post.setImageUrl("/uploads/posts/" + filename);
                            } catch (IOException e) {
                                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                                    "success", false,
                                    "message", "Failed to upload image: " + e.getMessage()
                                ));
                            }
                        }
                        
                        Post updatedPost = postRepository.save(post);
                        
                        return ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "Post updated successfully",
                            "data", PostDTO.fromEntity(updatedPost)
                        ));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update post: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // No-auth version of update with image
    @PutMapping("/noauth/{postId}/with-image")
    public ResponseEntity<?> updatePostWithImageNoAuth(
            @PathVariable Long postId,
            @RequestPart("content") String content,
            @RequestPart("user_id") String userIdStr,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        
        try {
            // Parse user ID
            Long userId = Long.valueOf(userIdStr);
            
            return postRepository.findById(postId)
                    .map(post -> {
                        // Check if the user is the author of the post
                        if (!post.getAuthor().getId().equals(userId)) {
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                                "success", false,
                                "message", "You are not authorized to update this post"
                            ));
                        }
                        
                        post.setContent(content);
                        
                        // Handle image update if provided
                        if (image != null && !image.isEmpty()) {
                            try {
                                // Create upload directory if it doesn't exist
                                File uploadDir = new File(UPLOAD_DIR);
                                if (!uploadDir.exists()) {
                                    uploadDir.mkdirs();
                                }
                                
                                // Delete old image if exists
                                if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
                                    String oldFilename = post.getImageUrl().substring(post.getImageUrl().lastIndexOf("/") + 1);
                                    Path oldFilePath = Paths.get(UPLOAD_DIR + oldFilename);
                                    Files.deleteIfExists(oldFilePath);
                                }
                                
                                // Generate unique filename for the image
                                String originalFilename = image.getOriginalFilename();
                                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                                String filename = UUID.randomUUID().toString() + fileExtension;
                                
                                // Save the file
                                Path filePath = Paths.get(UPLOAD_DIR + filename);
                                Files.write(filePath, image.getBytes());
                                
                                // Update image URL
                                post.setImageUrl("/uploads/posts/" + filename);
                            } catch (IOException e) {
                                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                                    "success", false,
                                    "message", "Failed to upload image: " + e.getMessage()
                                ));
                            }
                        }
                        
                        Post updatedPost = postRepository.save(post);
                        
                        return ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "Post updated successfully",
                            "data", PostDTO.fromEntity(updatedPost)
                        ));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update post: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // Delete a post
    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId) {
        try {
            return postRepository.findById(postId)
                    .map(post -> {
                        // Delete image file if exists
                        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
                            try {
                                String filename = post.getImageUrl().substring(post.getImageUrl().lastIndexOf("/") + 1);
                                Path filePath = Paths.get(UPLOAD_DIR + filename);
                                Files.deleteIfExists(filePath);
                            } catch (IOException e) {
                                // Log error but continue with post deletion
                                System.err.println("Failed to delete image file: " + e.getMessage());
                            }
                        }
                        
                        postRepository.delete(post);
                        
                        return ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "Post deleted successfully"
                        ));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to delete post: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // Search posts by content
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Post> postsPage = postRepository.findByContentContainingIgnoreCase(keyword, pageable);
            
            List<PostDTO> posts = postsPage.getContent().stream()
                    .map(PostDTO::fromEntity)
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("posts", posts);
            response.put("currentPage", postsPage.getNumber());
            response.put("totalItems", postsPage.getTotalElements());
            response.put("totalPages", postsPage.getTotalPages());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to search posts: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // Configure static resource handler for serving uploaded files
    @GetMapping("/uploads/posts/{filename:.+}")
    @ResponseBody
    public ResponseEntity<byte[]> serveFile(@PathVariable String filename) {
        try {
            Path file = Paths.get(UPLOAD_DIR + filename);
            byte[] resource = Files.readAllBytes(file);
            
            return ResponseEntity
                    .ok()
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
} 