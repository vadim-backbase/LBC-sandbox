package com.backbase.accesscontrol.repository;

import com.backbase.accesscontrol.domain.BusinessFunction;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessFunctionJpaRepository extends JpaRepository<BusinessFunction, String> {

    Optional<BusinessFunction> findById(String id);

}
