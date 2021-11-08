package com.backbase.accesscontrol.repository;

import com.backbase.accesscontrol.domain.AssignablePermissionSet;
import java.util.Optional;

public interface AssignablePermissionSetJpaCustomRepository {

    Optional<AssignablePermissionSet> findById(Long id, String entityGraphName);

    Optional<AssignablePermissionSet> findByName(String name, String entityGraphName);

}
