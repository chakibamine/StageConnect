package com.backend.stageconnect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "user_type")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    private String phone;
    
    private boolean enabled = true;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", insertable = false, updatable = false)
    private UserType userType;
    
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();
    
    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserFollowing> following = new ArrayList<>();
    
    @OneToMany(mappedBy = "followed", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserFollowing> followers = new ArrayList<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> likedPosts = new ArrayList<>();
    
    // Helper method to add a post
    public void addPost(Post post) {
        posts.add(post);
        post.setAuthor(this);
    }
    
    // Helper method to remove a post
    public void removePost(Post post) {
        posts.remove(post);
        post.setAuthor(null);
    }
    
    // Helper method to like a post
    public void likePost(Post post) {
        PostLike postLike = new PostLike();
        postLike.setId(new PostLike.PostLikeId(this.id, post.getId()));
        postLike.setUser(this);
        postLike.setPost(post);
        likedPosts.add(postLike);
    }
    
    // Helper method to unlike a post
    public void unlikePost(Post post) {
        likedPosts.removeIf(like -> 
            like.getId().getPostId().equals(post.getId()) && 
            like.getId().getUserId().equals(this.id)
        );
    }
} 