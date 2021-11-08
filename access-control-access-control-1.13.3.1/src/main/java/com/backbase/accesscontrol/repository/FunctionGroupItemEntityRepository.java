package com.backbase.accesscontrol.repository;

import com.backbase.accesscontrol.domain.FunctionGroupItemEntity;
import com.backbase.accesscontrol.domain.FunctionGroupItemId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FunctionGroupItemEntityRepository extends JpaRepository<FunctionGroupItemEntity, FunctionGroupItemId> {

}
