package com.aws.hack.repository;

import com.aws.hack.model.User;
import org.springframework.data.repository.CrudRepository;

/**
 * author: vyl
 * date: 14/07/2018
 */
public interface UserRepository extends CrudRepository<User, Long> {
}
