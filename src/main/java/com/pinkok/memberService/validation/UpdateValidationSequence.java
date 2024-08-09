package com.pinkok.memberService.validation;


import jakarta.validation.GroupSequence;

@GroupSequence({
        ValidationGroup.PasswordBlank.class,
        ValidationGroup.PasswordFormat.class,
})
public interface UpdateValidationSequence {
}
