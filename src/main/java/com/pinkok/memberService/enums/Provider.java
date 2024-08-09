package com.pinkok.memberService.enums;

import lombok.Getter;

@Getter
public enum Provider {

    GOOGLE("GOOGLE"),
    NAVER("NAVER"),
    KAKAO("KAKAO"),
    NULL("NULL");

    private final String provider;

    Provider(String provider){
        this.provider = provider;
    }


}
