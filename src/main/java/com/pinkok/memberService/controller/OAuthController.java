package com.pinkok.memberService.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.pinkok.memberService.dto.request.GoogleAccount;
import com.pinkok.memberService.dto.request.KakaoAccount;
import com.pinkok.memberService.dto.request.NaverAccount;
import com.pinkok.memberService.dto.response.MemberResponse;
import com.pinkok.memberService.service.OAuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/oauth")
public class OAuthController {

    private final OAuthService oAuthService;

    @PostMapping("/login/kakao")
    public ResponseEntity<MemberResponse> kakaoAccessLogin(@RequestBody KakaoAccount dto, HttpServletResponse response){

        MemberResponse result = oAuthService.kakaoLogin(dto, response);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/login/naver")
    public ResponseEntity<MemberResponse> naverAccessLogin(@RequestBody NaverAccount dto, HttpServletResponse response){

        MemberResponse result = oAuthService.naverLogin(dto, response);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/login/google")
    public ResponseEntity<MemberResponse> googleAccessLogin(@RequestBody GoogleAccount dto, HttpServletResponse response){

        MemberResponse result = oAuthService.googleLogin(dto, response);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
