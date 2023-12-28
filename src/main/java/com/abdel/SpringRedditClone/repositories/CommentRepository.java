package com.abdel.SpringRedditClone.repositories;

import com.abdel.SpringRedditClone.entities.Comment;
import com.abdel.SpringRedditClone.entities.Post;
import com.abdel.SpringRedditClone.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPost(Post post);

    List<Comment> findAllByUser(User user);
}