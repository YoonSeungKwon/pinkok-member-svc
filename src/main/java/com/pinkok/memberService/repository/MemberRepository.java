package com.pinkok.memberService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.pinkok.memberService.entity.Members;

import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<Members, Long> {

    Members findMembersByMemberIdx(long idx);

    Members findMembersByEmail(String email);

    Members findMembersByRefresh(String token);

    boolean existsMembersByUsername(String name);

    boolean existsMembersByEmail(String email);

    void deleteByMemberIdx(long idx);

    boolean existsMembersByMemberIdx(long idx);

    List<Members> findMembersByMemberIdxIn(List<Long> list);

}
