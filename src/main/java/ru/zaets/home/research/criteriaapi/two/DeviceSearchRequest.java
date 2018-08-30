package ru.zaets.home.research.criteriaapi.two;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceSearchRequest implements Serializable {

    private String name;
    private Set<UUID> groupIds;
    private Set<Boolean> markers;
}
