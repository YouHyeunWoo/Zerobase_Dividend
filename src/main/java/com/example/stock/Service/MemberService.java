package com.example.stock.Service;

import com.example.stock.exception.impl.AlreadyExistUserException;
import com.example.stock.model.Auth;
import com.example.stock.model.MemberEntity;
import com.example.stock.persist.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class MemberService implements UserDetailsService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails userDetails = this.memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("could not find user -> " + username));
        return userDetails;
    }

    public MemberEntity register(Auth.SignUp member) {
        boolean exists = this.memberRepository.existsByUsername(member.getUsername());

        if (exists) {
            throw new AlreadyExistUserException();
        }

        //사용자 비밀번호는 암호화를 하여 데이터베이스에 넣는다
        member.setPassword(this.passwordEncoder.encode(member.getPassword()));
        var result = this.memberRepository.save(member.toEntity());
        return result;
    }

    //패스워드 인증메소드
    public MemberEntity authenticate(Auth.SignIn member) {
        MemberEntity memberEntity = this.memberRepository.findByUsername(member.getUsername())
                .orElseThrow(() -> new RuntimeException("유저 아이디가 없습니다"));

        //memberEntity의 비밀번호는 암호화된 비밀번호, 입력받은 비밀번호는 일반 비밀번호
        //일치하지 않는경우
        if (!this.passwordEncoder.matches(member.getPassword(), memberEntity.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다");
        } else {
            //일치하는 경우
            return memberEntity;
        }
    }
}
