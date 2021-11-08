package com.backbase.accesscontrol.util.helpers;

import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ParticipantIngest;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TestDataUtils {

    @SafeVarargs
    public static <T> Set<T> getParticipants(T... participant) {
        return new LinkedHashSet<>(Arrays.asList(participant));
    }

    public static Set<String> getSetStrings(List<String> adminIds) {
        return new LinkedHashSet<>(adminIds);
    }

    public static ParticipantIngest getParticipant(String id, List<String> adminIds, boolean isSharingUsers,
        boolean isSharingAccounts, List<String> users) {
        return new ParticipantIngest()
            .withExternalId(id)
            .withAdmins(getSetStrings(adminIds))
            .withSharingUsers(isSharingUsers)
            .withSharingAccounts(isSharingAccounts)
            .withUsers(getSetStrings(users));
    }

    public static String getUuid() {
        return UUID.randomUUID().toString();
    }
}
