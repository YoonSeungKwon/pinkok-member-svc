package com.pinkok.memberService.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberResponse {

    private long idx;

    private String email;

    private String name;

    private String profile;

    private String createdAt;

    private String updatedAt;

}
