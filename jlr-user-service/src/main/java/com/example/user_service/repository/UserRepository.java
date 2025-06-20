package com.example.user_service.repository;


import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.example.user_service.entity.User;
import com.example.user_service.entity.UserRole;


import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByActiveTrue();

    List<User> findByRole(UserRole role);

    List<User> findByDealerId(String delaerId);

    //find dealer manager for a specific dealer

    @Query("SELECT u FROM User u WHERE u.dealerId = :dealerId AND u.role = 'DEALER_MANAGER'")
    List<User> findDealerManagersByDealerId(@Param("dealerId") String dealerId);





}
