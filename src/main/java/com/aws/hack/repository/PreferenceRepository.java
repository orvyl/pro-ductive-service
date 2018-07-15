package com.aws.hack.repository;

import com.aws.hack.model.Preference;
import org.springframework.data.repository.CrudRepository;

/**
 * author: vyl
 * date: 14/07/2018
 */
public interface PreferenceRepository extends CrudRepository<Preference, Long> {
    Preference findByPreference(String preference);
}
