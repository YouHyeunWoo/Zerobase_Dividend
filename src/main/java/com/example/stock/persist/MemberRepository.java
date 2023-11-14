package com.example.stock.persist;

import com.example.stock.persist.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

    //id를 기준으로 회원정보를 찾기
    @Query("select m from MEMBER m left join fetch m.roles")
    Optional<MemberEntity> findByUsername(String username);

    //회원가입할때 이미 존재하는 회원인지 확인
    boolean existsByUsername(String username);
}
