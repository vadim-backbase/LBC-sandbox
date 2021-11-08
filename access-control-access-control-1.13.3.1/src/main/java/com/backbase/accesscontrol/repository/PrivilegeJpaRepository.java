package com.backbase.accesscontrol.repository;

import com.backbase.accesscontrol.domain.Privilege;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrivilegeJpaRepository extends JpaRepository<Privilege, String> {


}
