package com.pinkok.memberService.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.pinkok.memberService.repository.MemberRepository;
import com.pinkok.memberService.security.JwtProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import com.pinkok.memberService.dto.request.GoogleAccount;
import com.pinkok.memberService.dto.request.KakaoAccount;
import com.pinkok.memberService.dto.request.NaverAccount;
import com.pinkok.memberService.dto.response.MemberResponse;
import com.pinkok.memberService.entity.Members;
import com.pinkok.memberService.enums.ExceptionCode;
import com.pinkok.memberService.enums.Provider;
import com.pinkok.memberService.enums.Role;
import com.pinkok.memberService.exception.UnAuthorizedException;
import com.pinkok.memberService.repository.MemberRepository;
import com.pinkok.memberService.security.JwtProvider;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private final JwtProvider jwtProvider;

    private final MemberRepository memberRepository;

    private final AmazonS3Client amazonS3Client;

    private final String bucket = "pinkok-storage";
    private final String region = "ap-northeast-2";

    @Value("${KAKAO_SECRET}")
    private String kakaoSecret;

    @Value("${KAKAO_ID}")
    private String kakaoId;

    @Value("${KAKAO_URI}")
    private String kakaoUri;

    @Value("${NAVER_ID}")
    private String naverId;

    @Value("${NAVER_SECRET}")
    private String naverSecret;

    @Value("${NAVER_URI}")
    private String naverUri;

    @Value("${GOOGLE_ID}")
    private String googleId;

    @Value("${GOOGLE_SECRET}")
    private String googleSecret;

    @Value("${GOOGLE_URI}")
    private String googleUri;

    private MemberResponse toResponse(Members members){
        return new MemberResponse(members.getMemberIdx(), members.getEmail(), members.getUsername()
                , members.getProfile(), String.valueOf(members.getCreatedAt()), String.valueOf(members.getUpdatedAt()));
    }

    public KakaoAccount getKakaoAccount(String token){
        RestTemplate template = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        headers.add("Authorization", "Bearer "+token);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = template.exchange("https://kapi.kakao.com/v2/user/me", HttpMethod.GET, entity, String.class);

        JSONObject json = new JSONObject(response.getBody());
        JSONObject account = (JSONObject) json.get("kakao_account");
        JSONObject profile = (JSONObject) account.get("profile");

        KakaoAccount kakaoAccount = new KakaoAccount(String.valueOf(account.get("email")),
                String.valueOf(profile.get("nickname")),
                String.valueOf(profile.get("profile_image_url")));

        return kakaoAccount;
    }

    @Transactional
    public MemberResponse kakaoLogin(KakaoAccount kakaoAccount, HttpServletResponse response){

//        RestTemplate template = new RestTemplate();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
//
//        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
//
//        body.add("grant_type", "authorization_code");
//        body.add("client_id", kakaoId);
//        body.add("redirect_uri", kakaoUri);
//        body.add("code", code);
//        body.add("client_secret", kakaoSecret);
//
//        HttpEntity<MultiValueMap<String, String>> kakaoRequest = new HttpEntity<>(body, headers);
//
//        ResponseEntity<String> kakaoResponse = template.exchange("https://kauth.kakao.com/oauth/token", HttpMethod.POST, kakaoRequest, String.class);
//
//        JSONObject json = new JSONObject(kakaoResponse.getBody());

        if(memberRepository.existsMembersByEmail(kakaoAccount.getEmail())
                && memberRepository.findMembersByEmail(kakaoAccount.getEmail()).getProvider() != Provider.KAKAO)
            throw new UnAuthorizedException(ExceptionCode.EMAIL_ALREADY_EXISTS.getMessage(), ExceptionCode.EMAIL_ALREADY_EXISTS.getStatus());

        if(!memberRepository.existsMembersByEmail(kakaoAccount.getEmail()))
            kakaoRegister(kakaoAccount);


        Members members = memberRepository.findMembersByEmail(kakaoAccount.getEmail());

        String accToken = jwtProvider.createAccessToken(members.getEmail());
        String refToken = jwtProvider.createRefreshToken();

        response.addHeader("Authorization", accToken);
        response.addHeader("X-Refresh-Token", refToken);

        members.setRefresh(refToken);

        return toResponse(members);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void kakaoRegister(KakaoAccount account){

        Members members = Members.builder()
                .email(account.getEmail())
                .password(null)
                .name(account.getName())
                .phone(null)
                .role(Role.USER)
                .build();
        members.setProvider(Provider.KAKAO);
        members.setProfile(account.getProfile());
        members.setOauth(true);

        memberRepository.save(members);
    }

    public NaverAccount getNaverAccount(String token){
        RestTemplate template = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        headers.add("Authorization", "Bearer "+token);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = template.exchange("https://openapi.naver.com/v1/nid/me", HttpMethod.GET, entity, String.class);

        JSONObject json = new JSONObject(response.getBody());
        System.out.println(json);
        JSONObject account = (JSONObject) json.get("response");

        NaverAccount naverAccount = new NaverAccount(String.valueOf(account.get("email")),
                String.valueOf(account.get("name")),
                String.valueOf(account.get("profile_image")));

        return naverAccount;
    }

    @Transactional
    public MemberResponse naverLogin(NaverAccount naverAccount, HttpServletResponse response){
//        RestTemplate template = new RestTemplate();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
//
//        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
//
//        body.add("grant_type", "authorization_code");
//        body.add("client_id", naverId);
//        body.add("redirect_uri", naverUri);
//        body.add("code", code);
//        body.add("client_secret", naverSecret);
//        body.add("state", "state");
//
//        HttpEntity<MultiValueMap<String, String>> naverRequest = new HttpEntity<>(body, headers);
//
//        System.out.println(code);
//
//        ResponseEntity<String> naverResponse = template.exchange("https://nid.naver.com/oauth2.0/token", HttpMethod.POST, naverRequest, String.class);
//
//        JSONObject json = new JSONObject(naverResponse.getBody());
//        String token = (String) json.get("access_token");

//        NaverAccount naverAccount = dto;

        if(memberRepository.existsMembersByEmail(naverAccount.getEmail())
                && memberRepository.findMembersByEmail(naverAccount.getEmail()).getProvider() != Provider.NAVER)
            throw new UnAuthorizedException(ExceptionCode.EMAIL_ALREADY_EXISTS.getMessage(), ExceptionCode.EMAIL_ALREADY_EXISTS.getStatus());

        if(!memberRepository.existsMembersByEmail(naverAccount.getEmail()))
            naverRegister(naverAccount);


        Members members = memberRepository.findMembersByEmail(naverAccount.getEmail());

        String accToken = jwtProvider.createAccessToken(members.getEmail());
        String refToken = jwtProvider.createRefreshToken();

        response.addHeader("Authorization", accToken);
        response.addHeader("X-Refresh-Token", refToken);

        members.setRefresh(refToken);

        return toResponse(members);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void naverRegister(NaverAccount account){

        Members members = Members.builder()
                .email(account.getEmail())
                .password(null)
                .name(account.getName())
                .phone(null)
                .role(Role.USER)
                .build();
        members.setProvider(Provider.NAVER);
        members.setOauth(true);
        members.setProfile(account.getProfile());

        memberRepository.save(members);
    }

    public GoogleAccount getGoogleAccount(String token){
        RestTemplate template = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        headers.add("Authorization", "Bearer "+token);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = template.exchange("https://www.googleapis.com/oauth2/v3/userinfo", HttpMethod.GET, entity, String.class);

        JSONObject account = new JSONObject(response.getBody());

        GoogleAccount googleAccount = new GoogleAccount(String.valueOf(account.get("email")),
                String.valueOf(account.get("name")),
                String.valueOf(account.get("picture")));

        return googleAccount;
    }

    @Transactional
    public MemberResponse googleLogin(GoogleAccount googleAccount, HttpServletResponse response){

//        RestTemplate template = new RestTemplate();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
//
//        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
//
//        body.add("grant_type", "authorization_code");
//        body.add("client_id", googleId);
//        body.add("redirect_uri", googleUri);
//        body.add("code", code);
//        body.add("client_secret", googleSecret);
//
//        HttpEntity<MultiValueMap<String, String>> naverRequest = new HttpEntity<>(body, headers);
//
//        System.out.println(code);
//
//        ResponseEntity<String> naverResponse = template.exchange("https://oauth2.googleapis.com/token", HttpMethod.POST, naverRequest, String.class);
//
//        JSONObject json = new JSONObject(naverResponse.getBody());
//        String token = (String) json.get("access_token");
//
//        GoogleAccount googleAccount = getGoogleAccount(token);
        System.out.println();
        if(memberRepository.existsMembersByEmail(googleAccount.getEmail())
                && memberRepository.findMembersByEmail(googleAccount.getEmail()).getProvider() != Provider.GOOGLE)
            throw new UnAuthorizedException(ExceptionCode.EMAIL_ALREADY_EXISTS.getMessage(), ExceptionCode.EMAIL_ALREADY_EXISTS.getStatus());

        if(!memberRepository.existsMembersByEmail(googleAccount.getEmail()))
            googleRegister(googleAccount);


        Members members = memberRepository.findMembersByEmail(googleAccount.getEmail());

        String accToken = jwtProvider.createAccessToken(members.getEmail());
        String refToken = jwtProvider.createRefreshToken();

        response.addHeader("Authorization", accToken);
        response.addHeader("X-Refresh-Token", refToken);

        members.setRefresh(refToken);

        return toResponse(members);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void googleRegister(GoogleAccount account){

        Members members = Members.builder()
                .email(account.getEmail())
                .password(null)
                .name(account.getName())
                .phone(null)
                .role(Role.USER)
                .build();
        members.setProvider(Provider.GOOGLE);
        members.setOauth(true);
        members.setProfile(account.getProfile());

        memberRepository.save(members);
    }


}
