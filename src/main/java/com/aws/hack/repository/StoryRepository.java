package com.aws.hack.repository;

import com.aws.hack.model.Story;
import org.springframework.data.repository.CrudRepository;

/**
 * author: vyl
 * date: 14/07/2018
 */
public interface StoryRepository extends CrudRepository<Story, Long> {
}
