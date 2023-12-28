package com.abdel.SpringRedditClone.repositories;

import com.abdel.SpringRedditClone.entities.Post;
import com.abdel.SpringRedditClone.entities.User;
import com.abdel.SpringRedditClone.entities.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findTopByPostAndUserOrderByVoteIdDesc(Post post, User currentUser);
}