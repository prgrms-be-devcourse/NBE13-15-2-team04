package com.team04.playlist.user;

import com.team04.playlist.global.exception.BusinessException;
import com.team04.playlist.global.exception.ErrorCode;
import com.team04.playlist.user.dto.UserSummary;
import com.team04.playlist.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    //TODO: 자기 자신 정보를 가져와 dto 로 가공해 반환
    public void getMe(){

    }

    //TODO: 유저 엔티티를 불러온 다음 닉네임을 변경후 저장
    @Transactional
    public void updateNickname(){

    }

    //TODO: 유저 닉네임,아이디 가져옴
    public UserSummary getUserSummary(Long userId){
        return UserSummary.from(this.getUser(userId));
    }

    private User getUser(Long id){
        return userRepository.findById(id).orElseThrow(
                () -> new BusinessException(ErrorCode.USER_NOT_FOUND)
        );
    }
}
