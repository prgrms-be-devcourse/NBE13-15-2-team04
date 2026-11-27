package com.team04.playlist.user;

import com.team04.playlist.global.exception.BusinessException;
import com.team04.playlist.global.exception.ErrorCode;
import com.team04.playlist.user.dto.UserSummary;
import com.team04.playlist.user.entity.Provider;
import com.team04.playlist.user.entity.User;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {
    private static final int NICKNAME_RETRY_COUNT = 5;
    // 닉네임 중복 시 뒤에 붙이는 랜덤 숫자의 자릿수 (4 → 1000~9999)
    private static final int NICKNAME_SUFFIX_DIGITS = 4;

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

    // 소셜 로그인: 이미 가입한 사용자면 조회, 처음이면 가입시킨다.
    @Transactional
    public User findOrCreate(Provider provider, String providerId, String nickname) {
        return userRepository.findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> userRepository.save(User.builder()
                        .provider(provider)
                        .providerId(providerId)
                        .nickname(generateUniqueNickname(nickname))
                        .build()));
    }

    // 소셜 닉네임을 기반으로 중복되지 않는 닉네임을 만든다.
    // 이미 사용 중이면 뒤에 4자리 숫자를 붙여 재시도하고, 그래도 겹치면 랜덤 닉네임을 쓴다.
    private String generateUniqueNickname(String nickname) {
        String base = (nickname == null || nickname.isBlank()) ? User.DEFAULT_NICKNAME : nickname.strip();
        base = truncate(base, User.NICKNAME_MAX_LENGTH);
        if (!userRepository.existsByNickname(base)) {
            return base;
        }

        // 랜덤 숫자를 붙여도 최대 길이를 넘지 않도록 숫자 자릿수만큼 앞부분을 자른다.
        String prefix = truncate(base, User.NICKNAME_MAX_LENGTH - NICKNAME_SUFFIX_DIGITS);
        int suffixMin = (int) Math.pow(10, NICKNAME_SUFFIX_DIGITS - 1);
        int suffixMax = suffixMin * 10;
        for (int i = 0; i < NICKNAME_RETRY_COUNT; i++) {
            String candidate = prefix + ThreadLocalRandom.current().nextInt(suffixMin, suffixMax);
            if (!userRepository.existsByNickname(candidate)) {
                return candidate;
            }
        }

        // 기본 닉네임 뒤에 남은 길이만큼 랜덤 문자를 붙인다.
        String random = UUID.randomUUID().toString().replace("-", "");
        int randomLength = Math.min(User.NICKNAME_MAX_LENGTH - User.DEFAULT_NICKNAME.length(), random.length());
        return User.DEFAULT_NICKNAME + random.substring(0, randomLength);
    }

    // DB 컬럼(VARCHAR(20))은 글자 수 기준이라 코드 포인트 단위로 자른다. (이모지 등이 깨지지 않도록)
    private static String truncate(String value, int maxLength) {
        if (value.codePointCount(0, value.length()) <= maxLength) {
            return value;
        }
        return value.substring(0, value.offsetByCodePoints(0, maxLength));
    }

    private User getUser(Long id){
        return userRepository.findById(id).orElseThrow(
                () -> new BusinessException(ErrorCode.USER_NOT_FOUND)
        );
    }
}
