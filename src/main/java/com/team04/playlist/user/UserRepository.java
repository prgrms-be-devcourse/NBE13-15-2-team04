package com.team04.playlist.user;

import com.team04.playlist.user.entity.Provider;
import com.team04.playlist.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

    boolean existsByNickname(String nickname);
}
