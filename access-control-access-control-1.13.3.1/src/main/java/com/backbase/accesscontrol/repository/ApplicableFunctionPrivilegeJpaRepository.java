package com.backbase.accesscontrol.repository;

import static com.backbase.accesscontrol.domain.GraphConstants.APPLICABLE_FUNCTION_PRIVILEGE_WITH_BUSINESS_FUNCTION_AND_PRIVILEGE;

import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicableFunctionPrivilegeJpaRepository extends JpaRepository<ApplicableFunctionPrivilege, String> {

    @EntityGraph(value = APPLICABLE_FUNCTION_PRIVILEGE_WITH_BUSINESS_FUNCTION_AND_PRIVILEGE,
        type = EntityGraph.EntityGraphType.FETCH)
    List<ApplicableFunctionPrivilege> findAll();
}
