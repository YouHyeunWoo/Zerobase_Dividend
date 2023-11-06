package com.example.stock.web;

import com.example.stock.Service.MemberService;
import com.example.stock.model.Auth;
import com.example.stock.model.MemberEntity;
import com.example.stock.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/auth")
@RequiredArgsConstructor
@RestController
public class AuthController {
    private final MemberService memberService;

    private final TokenProvider tokenProvider;

    //회원가입을 위한 api
    //Auth.signUp 타입의 아이디, 패스워드, 사용자권한이 요청으로 들어오면
    //아이디가 존재하는 아이디인지 확인해서 존재하면 회원가입 실패
    //존재하지 않는 아이디이면 비밀번호를 인코딩(암호화)한 값을 MEMBER테이블에 저장
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Auth.SignUp request) {
        var result = this.memberService.register(request);
        return ResponseEntity.ok(result);
    }

    //로그인 전용 api
    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody Auth.SignIn request) {
        //1. SignIn으로 받은 아이디와 비밀번호가 일치하는지 확인(패스워드 검증)
        MemberEntity memberEntity = this.memberService.authenticate(request);

        //2. 토큰을 생성해서 반환
        var token = tokenProvider
                .generateToken(memberEntity.getUsername()
                        , memberEntity.getRoles());

        log.info("user login -> " + request.getUsername());

        return ResponseEntity.ok(token);
    }
}
