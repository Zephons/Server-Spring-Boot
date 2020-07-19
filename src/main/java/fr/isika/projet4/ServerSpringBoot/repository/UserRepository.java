package fr.isika.projet4.ServerSpringBoot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.isika.projet4.ServerSpringBoot.domain.User;

public interface UserRepository extends JpaRepository<User, Long>{
	
	User findUserByUserName(String userName);
	
	User findUserByEmail(String email);
}
