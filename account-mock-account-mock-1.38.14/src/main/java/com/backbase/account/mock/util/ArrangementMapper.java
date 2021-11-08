package com.backbase.account.mock.util;

import com.backbase.mock.outbound.account.link.model.ArrangementItemDto;
import com.backbase.mock.outbound.details.model.ArrangementDetailsDto;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",
    imports = {Collectors.class})
public interface ArrangementMapper {

    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "bankAlias", constant = "bankAlias")
    @Mapping(target = "parentId", ignore = true)
    ArrangementItemDto map(ArrangementDetailsDto arrangementDetails, String id, String productId);
}
