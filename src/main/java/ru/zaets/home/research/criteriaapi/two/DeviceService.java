package ru.zaets.home.research.criteriaapi.two;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DeviceService {
    private final static Specification<Device> NULL_SPEC = (r, q, b) -> null;

    @Autowired
    DeviceRepository deviceRepository;
    private Set<Boolean> accessMarker;

    public static Specification<Device> getSpec(DeviceSearchRequest request) {
        return getAndSpec(request);
    }

    private static Specification<Device> getAndSpec(DeviceSearchRequest request) {

        return groupsSpec(request.getGroupIds());
    }


    private static Specification<Device> groupsSpec(Set<UUID> groups) {
        if (groups == null) { //select all devices
            return NULL_SPEC;
        } else if (groups.isEmpty()) { //select devices where group is null
            return (r, q, b) -> b.isEmpty(r.get(Device_.groups));
        } else { //select devices where group in (group_list)
            return (r, q, b) -> {
                final Set<Fetch<Device, ?>> fetches = r.getFetches();
                for (Fetch<Device, ?> fetch : fetches) {
                    if (fetch.getAttribute().equals(Device_.groups)) {
                        final Join<Device, Groups> join = (Join<Device, Groups>) fetch;
                        return join.get(Groups_.id).in(groups);
                    }
                }

                final Set<Join<Device, ?>> joins = r.getJoins();

                for (Join<Device, ?> join : joins) {
                    if (join.getAttribute().equals(Device_.groups)) {
                        final Join<Device, Groups> join1 = (Join<Device, Groups>) join;
                        return join1.get(Groups_.id).in(groups);
                    }
                }

                return r.join(Device_.groups).get(Groups_.id).in(groups);
            };
        }
    }

    @Transactional
    public Page<Device> searchProfilesPaged(DeviceSearchRequest request, Pageable pageable) {
        System.out.println();
        System.out.println(request);
        System.out.println(pageable);
        System.out.println();
        return deviceRepository.findAll(getSpec(request).and(buildGroupFilterWorked(request.getMarkers())), pageable);
    }


    @Transactional
    public List<Device> searchProfiles(DeviceSearchRequest request, Sort sort) {
        System.out.println();
        System.out.println(request);
        System.out.println(sort);
        System.out.println();
        return deviceRepository.findAll(getSpec(request).and(buildGroupFilterWorked(request.getMarkers())), sort);
    }


    public <T> Specification<T> buildMarkerFilter() {
        return (r, q, b) -> r.get("marker").in(accessMarker);
    }

    public <T> Specification<T> buildMarkerFilter(Collection<Boolean> markers) {
        if (markers == null || markers.isEmpty()) return buildMarkerFilter();

        Set<Boolean> resultMarker = accessMarker.stream().filter(markers::contains).collect(Collectors.toSet());


        if (resultMarker.isEmpty()) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.disjunction();
        }

        return (Root<T> r, CriteriaQuery<?> q, CriteriaBuilder b) -> {

            return r.get("domain").get("id").in(resultMarker);
        };
    }

    public void setAccessMarker(Set<Boolean> accessMarker) {
        this.accessMarker = accessMarker;
    }

    public Specification<Device> buildGroupFilterWorked(Collection<Boolean> markers) {

        Set<Boolean> queryDomains;
        if (markers == null || markers.isEmpty()) {
            queryDomains = accessMarker;
        } else {
            Set<Boolean> resultIds = accessMarker.stream().filter(markers::contains).collect(Collectors.toSet());
            if (resultIds.isEmpty()) {
                return (root, query, criteriaBuilder) -> criteriaBuilder.disjunction();
            } else {
                queryDomains = resultIds;
            }
        }

        return (Specification<Device>) (r, q, b) -> {
            if (q.getResultType() == Long.class || q.getResultType() == long.class) {
                // branch for count request
                q.distinct(true);
                final SetJoin<Device, Groups> join = r.join(Device_.groups, JoinType.LEFT);

                return b.or(
                        join.get(Groups_.marker).in(queryDomains),
                        b.and(
                                b.isNull(join.get(Groups_.id)),
                                join.get(Groups_.marker).in(queryDomains)
                        )
                );
            } else {
                final Join<Device, Groups> fetch = (Join<Device, Groups>) r.fetch(Device_.groups, JoinType.LEFT);

                return b.or(
                        fetch.get(Groups_.marker).in(queryDomains),
                        b.and(
                                b.isNull(fetch.get(Groups_.id)),
                                fetch.get(Groups_.marker).in(queryDomains)
                        )
                );
            }
        };
    }
}
