package com.backend.stageconnect.controller;

import com.backend.stageconnect.dto.EnhancedPostDTO;
import com.backend.stageconnect.dto.PostDTO;
import com.backend.stageconnect.entity.Comment;
import com.backend.stageconnect.entity.Post;
import com.backend.stageconnect.entity.PostLike;
import com.backend.stageconnect.entity.User;
import com.backend.stageconnect.repository.CommentRepository;
import com.backend.stageconnect.repository.PostLikeRepository;
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
import java.util.Collections;
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
    
    @Autowired
    private PostLikeRepository postLikeRepository;
    
    @Autowired
    private CommentRepository commentRepository;
    
    // Get the current user's feed (their posts and posts from users they follow)
    @GetMapping("/feed")
    public ResponseEntity<Map<String, Object>> getFeed(
            @RequestParam("user_id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "User not found with ID: " + userId);
                return ResponseEntity.badRequest().body(response);
            }
            
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            
            Page<Post> postsPage = postRepository.findFeedPostsForUser(user.getId(), pageable);
            
            // Get all post IDs liked by this user
            final List<Long> likedPostIds = postLikeRepository.findPostIdsByUserId(userId);
            
            // Map posts to enhanced DTOs with comments and likes
            List<EnhancedPostDTO> posts = postsPage.getContent().stream()
                    .map(post -> {
                        // Get all comments for this post
                        List<Comment> comments = commentRepository.findByPostIdAndParentCommentIsNullOrderByCreatedAtDesc(
                                post.getId(), Pageable.unpaged()).getContent();
                        
                        // Get all likes for this post
                        List<PostLike> likes = postLikeRepository.findByIdPostId(post.getId());
                        
                        return EnhancedPostDTO.fromEntity(post, comments, likes, likedPostIds);
                    })
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
    
    // Get all posts (for admin or public timeline) with detailed information
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllPosts(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Post> postsPage = postRepository.findAllByOrderByCreatedAtDesc(pageable);
            
            // Get user's liked posts if userId is provided
            final List<Long> likedPostIds = userId != null 
                ? postLikeRepository.findPostIdsByUserId(userId) 
                : null;
            
            // Map posts to enhanced DTOs with comments and likes
            List<EnhancedPostDTO> posts = postsPage.getContent().stream()
                    .map(post -> {
                        // Get all comments for this post
                        List<Comment> comments = commentRepository.findByPostIdAndParentCommentIsNullOrderByCreatedAtDesc(
                                post.getId(), Pageable.unpaged()).getContent();
                        
                        // Get all likes for this post
                        List<PostLike> likes = postLikeRepository.findByIdPostId(post.getId());
                        
                        return EnhancedPostDTO.fromEntity(post, comments, likes, likedPostIds);
                    })
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
            @RequestParam(required = false) Long currentUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Post> postsPage = postRepository.findByAuthorId(userId, pageable);
            
            // Get current user's liked posts if provided
            final List<Long> likedPostIds = currentUserId != null 
                ? postLikeRepository.findPostIdsByUserId(currentUserId) 
                : null;
            
            // Map posts to enhanced DTOs with comments and likes
            List<EnhancedPostDTO> posts = postsPage.getContent().stream()
                    .map(post -> {
                        // Get all comments for this post
                        List<Comment> comments = commentRepository.findByPostIdAndParentCommentIsNullOrderByCreatedAtDesc(
                                post.getId(), Pageable.unpaged()).getContent();
                        
                        // Get all likes for this post
                        List<PostLike> likes = postLikeRepository.findByIdPostId(post.getId());
                        
                        return EnhancedPostDTO.fromEntity(post, comments, likes, likedPostIds);
                    })
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
    
    // Get a specific post with detailed information
    @GetMapping("/{postId}")
    public ResponseEntity<?> getPost(
            @PathVariable Long postId,
            @RequestParam(required = false) Long userId) {
        try {
            return postRepository.findById(postId)
                    .map(post -> {
                        // Get liked status if userId is provided
                        final List<Long> likedPostIds = userId != null 
                            ? postLikeRepository.findPostIdsByUserId(userId) 
                            : null;
                        
                        // Get all comments for this post
                        List<Comment> comments = commentRepository.findByPostIdAndParentCommentIsNullOrderByCreatedAtDesc(
                                post.getId(), Pageable.unpaged()).getContent();
                        
                        // Get all likes for this post
                        List<PostLike> likes = postLikeRepository.findByIdPostId(post.getId());
                        
                        return ResponseEntity.ok(EnhancedPostDTO.fromEntity(post, comments, likes, likedPostIds));
                    })
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
            
            // Get all comments for this post (will be empty for new post)
            List<Comment> comments = Collections.emptyList();
            
            // Get all likes for this post (will be empty for new post)
            List<PostLike> likes = Collections.emptyList();
            
            // Create a singleton list with just this post's ID
            List<Long> likedPostIds = List.of();
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Post created successfully",
                "data", EnhancedPostDTO.fromEntity(savedPost, comments, likes, likedPostIds)
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
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Post> postsPage = postRepository.findByContentContainingIgnoreCase(keyword, pageable);
            
            // Get liked posts if userId is provided
            final List<Long> likedPostIds = userId != null 
                ? postLikeRepository.findPostIdsByUserId(userId) 
                : null;
            
            // Map posts to enhanced DTOs with comments and likes
            List<EnhancedPostDTO> posts = postsPage.getContent().stream()
                    .map(post -> {
                        // Get all comments for this post
                        List<Comment> comments = commentRepository.findByPostIdAndParentCommentIsNullOrderByCreatedAtDesc(
                                post.getId(), Pageable.unpaged()).getContent();
                        
                        // Get all likes for this post
                        List<PostLike> likes = postLikeRepository.findByIdPostId(post.getId());
                        
                        return EnhancedPostDTO.fromEntity(post, comments, likes, likedPostIds);
                    })
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

    // Get all post IDs liked by a user
    @GetMapping("/liked-by/{userId}")
    public ResponseEntity<?> getPostsLikedByUser(@PathVariable Long userId) {
        try {
            if (!userRepository.existsById(userId)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found with ID: " + userId
                ));
            }
            
            List<Long> likedPostIds = postLikeRepository.findPostIdsByUserId(userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Liked posts retrieved successfully",
                "data", likedPostIds
            ));
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get liked posts: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // Check if a user has liked a specific post
    @GetMapping("/{postId}/liked-by/{userId}")
    public ResponseEntity<?> checkIfUserLikedPost(
            @PathVariable Long postId,
            @PathVariable Long userId) {
        try {
            boolean hasLiked = postLikeRepository.existsByIdUserIdAndIdPostId(userId, postId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "hasLiked", hasLiked
            ));
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to check like status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // Like a post
    @PostMapping("/{postId}/like")
    public ResponseEntity<?> likePost(
            @PathVariable Long postId,
            @RequestBody Map<String, Object> requestBody) {
        
        try {
            // Extract values from request body
            Long userId = Long.valueOf(requestBody.get("user_id").toString());
            
            // Check if user exists
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found with ID: " + userId
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
            
            // Check if already liked
            if (postLikeRepository.existsByIdUserIdAndIdPostId(userId, postId)) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Post already liked",
                    "likeCount", post.getLikeCount()
                ));
            }
            
            // Create new like record
            PostLike postLike = new PostLike();
            postLike.setId(new PostLike.PostLikeId(userId, postId));
            postLike.setUser(user);
            postLike.setPost(post);
            postLikeRepository.save(postLike);
            
            // Update post like count
            post.setLikeCount(post.getLikeCount() + 1);
            Post updatedPost = postRepository.save(post);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Post liked successfully",
                "likeCount", updatedPost.getLikeCount()
            ));
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to like post: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Unlike a post
    @PostMapping("/{postId}/unlike")
    public ResponseEntity<?> unlikePost(
            @PathVariable Long postId,
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
            
            // Check if post exists
            Post post = postRepository.findById(postId).orElse(null);
            if (post == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Post not found with ID: " + postId
                ));
            }
            
            // Check if like exists
            if (!postLikeRepository.existsByIdUserIdAndIdPostId(userId, postId)) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Post is not liked by this user",
                    "likeCount", post.getLikeCount()
                ));
            }
            
            // Delete like
            postLikeRepository.deleteByIdUserIdAndIdPostId(userId, postId);
            
            // Update post like count
            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
            Post updatedPost = postRepository.save(post);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Post unliked successfully",
                "likeCount", updatedPost.getLikeCount()
            ));
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to unlike post: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
} 