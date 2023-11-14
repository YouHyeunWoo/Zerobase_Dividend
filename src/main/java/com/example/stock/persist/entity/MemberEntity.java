package com.example.stock.persist.entity;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "MEMBER")
@Builder
public class MemberEntity implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String password;

    @ElementCollection
    private List<String> roles;

    //연관 관계로 설정되어 있는 roles를 시큐리티에 맞는 객체로 매핑해주는데 이때 JPA의 지연
    //로딩으로 인해 roles값이 없는 상태여서 나는 에러로 예상
    //또는 영속성 컨텍스트가 종료된 상태에서 조회를 하여 발생했을수도있음
    // repository에서 @Query 애노테이션을 사용하여 jpql로 직접 쿼리를 작성해주시면 됩니다.
    //MEMBER테이블을 조회할때 fetch join으로 연관관계가 있는 엔티티를 모두 조회
    //MemberRepository에서 유저를 조회할때 연관관계가 있는 roles 정보도 같이 조회
    //@Query(“select m from member m left join fetch m.roles”)
    //Optional<MemberEntity> findByUsernameFetchJoin(String username);
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
