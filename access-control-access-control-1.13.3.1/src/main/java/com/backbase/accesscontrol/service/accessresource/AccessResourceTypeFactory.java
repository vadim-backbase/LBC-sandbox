package com.backbase.accesscontrol.service.accessresource;

import com.backbase.accesscontrol.domain.Participant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public enum AccessResourceTypeFactory {

    NONE {
        @Override
        public List<String> getValidResources(List<Participant> participants, String userLegalEntityId) {
            return new ArrayList<>();
        }
    },
    USER {
        @Override
        public List<String> getValidResources(List<Participant> participants, String userLegalEntityId) {
            List<String> result = new ArrayList<>();
            List<String> participantsSharingUsers = participants
                .stream()
                .filter(Participant::isShareUsers)
                .map(participant -> participant.getLegalEntity().getId())
                .collect(Collectors.toList());

            if (participantsSharingUsers.stream().anyMatch(leId -> leId.equals(userLegalEntityId))) {
                result.addAll(participantsSharingUsers);
            }
            return result;
        }
    },
    ACCOUNT {
        @Override
        public List<String> getValidResources(List<Participant> participants, String userLegalEntityId) {
            List<String> result = new ArrayList<>();
            List<String> participantsSharingAccounts = participants
                .stream()
                .filter(Participant::isShareAccounts)
                .map(participant -> participant.getLegalEntity().getId())
                .collect(Collectors.toList());

            if (participantsSharingAccounts.contains(userLegalEntityId)) {
                result.addAll(participantsSharingAccounts);
            }
            return result;

        }
    },
    USER_OR_ACCOUNT {
        @Override
        public List<String> getValidResources(List<Participant> participants, String userLegalEntityId) {
            List<String> result = new ArrayList<>();
            List<String> participantsSharingAccounts = participants
                .stream()
                .filter(Participant::isShareAccounts)
                .map(participant -> participant.getLegalEntity().getId())
                .collect(Collectors.toList());

            if (participantsSharingAccounts.contains(userLegalEntityId)) {
                result.addAll(participantsSharingAccounts);
            }

            List<String> participantsSharingUsers = participants
                .stream()
                .filter(Participant::isShareUsers)
                .map(participant -> participant.getLegalEntity().getId())
                .collect(Collectors.toList());

            if (participantsSharingUsers.contains(userLegalEntityId)) {
                result.addAll(participantsSharingUsers);
            }
            return result.stream().distinct().collect(Collectors.toList());
        }

    },
    USER_AND_ACCOUNT {
        @Override
        public List<String> getValidResources(List<Participant> participants, String userLegalEntityId) {

            List<String> participantsIds = participants.stream()
                .map(participant -> participant.getLegalEntity().getId())
                .collect(Collectors.toList());
            if (!participantsIds.contains(userLegalEntityId)) {
                return new ArrayList<>();
            }
            return participantsIds;
        }
    };

    public abstract List<String> getValidResources(List<Participant> participants, String userLegalEntityId);
}
