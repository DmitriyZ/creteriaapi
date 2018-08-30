package ru.zaets.home.research.criteriaapi.two;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import ru.zaets.home.research.criteriaapi.ApplicationConfig;

import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

/**
 * Тест 1
 * Получить Device с только с отфильтрованными группами (+ сортировка по подсущностям)
 * Два пользователя:
 * 1. маркер (true, false)
 * 3. только маркер in (true)
 * Получить сразу все сущности
 * Получить по страницам
 *
 */
@Slf4j
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ApplicationConfig.class})
@PropertySource("classpath*:application.properties")
@PropertySource("classpath*:hibernate.properties")
public class CriteriaapiApplicationTest {

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    DeviceRepository deviceRepository;

    @Autowired
    DeviceService  deviceService;

    @Autowired
    EntityManagerFactory entityManagerFactory;
    private Groups groups6;

    private static Specification<Device> simpleJoin() {
        return (Specification<Device>) (r, q, b) -> {
            final SetJoin<Device, Groups> join = r.join(Device_.groups);
//            q.multiselect(join);
            q.distinct(true);
            return b.equal(join.get(Groups_.marker), r.get(Device_.marker));
        };
    }

    private Specification<Device> addJoinedTableInSelect() {
        return (Specification<Device>) (r, q, b) -> {
            q.distinct(true);
            r.join(Device_.groups);
            final Join<Device, Groups> fetch = (Join<Device, Groups>) r.fetch(Device_.groups);
            return b.equal(fetch.get(Groups_.marker), r.get(Device_.marker));
        };
    }

//    private Specification<Device> d() {
//        return (Specification<Device>) (r, q, b) -> {
//            if (q.getResultType() == Long.class || q.getResultType() == long.class) {
//                // branch for count request
//                q.distinct(true);
//                final SetJoin<Device, Groups> join = r.join(Device_.items, JoinType.LEFT);
//
//                return b.or(
//                        join.get(Device_.).get(ClientDomain_.id).in(queryDomains),
//                        b.and(
//                                b.isNull(join.get(Device_.id)),
//                                join.get(Device_.domain).get(ClientDomain_.id).in(queryDomains)
//                        )
//                );
//            } else {
//                final Join<Device, Groups> fetch = (Join<Device, Groups>) r.fetch(DeviceProfile_.groups, JoinType.LEFT);
//
//                q.orderBy(b.asc(fetch.get(Device_.id)));
//
//                return b.or(
//                        fetch.get(Device_.domain).get(ClientDomain_.id).in(queryDomains),
//                        b.and(
//                                b.isNull(fetch.get(Device_.id)),
//                                fetch.get(Device_.domain).get(ClientDomain_.id).in(queryDomains)
//                        )
//                );
//            }
//        };
//    }

//    private static Specification<Device> addJoinedTableInSelect1() {
//        return (Specification<Device>) (r, q, b) -> {
//            List<Predicate> predicates = new ArrayList<>();
//
//            q.distinct(true);
//            final Join<Device, Groups> join = (Join<Device, Groups>) r.fetch(Device_.groups);
//            predicates.add(b.equal(join.get(Groups_.marker), r.get(Device_.marker)));
//
//            return b.and(predicates.toArray(new Predicate[predicates.size()]));
//        };
//    }

    @Before
    public void joinTest() {
        groupRepository.save(Groups.builder().name("big group").marker(true).build());
        groupRepository.save(Groups.builder().name("small group").marker(false).build());

        final Groups groups1 = Groups.builder().name("group 1").marker(false).build();
        // Device 1
        {
            final Groups groups2 = Groups.builder().name("group 2").marker(true).build();
            final Groups groups3 = Groups.builder().name("group 3").marker(false).build();
            final Groups groups4 = Groups.builder().name("group 4").marker(true).build();

            final Set<Groups> groups = new HashSet<>(Arrays.asList(groups1, groups2, groups3, groups4));
            final List<Groups> groups5 = groupRepository.saveAll(groups);
            groups6 = groups5.get(0);

            final Device device = Device.builder().name("device 1").groups(groups).marker(true).build();
            deviceRepository.save(device);
        }

        // Device 2
        {
            final Groups groups2F = Groups.builder().name("xxxxxxxxxxxx").marker(false).build();

            final Set<Groups> groupsF = new HashSet<>(Arrays.asList(groups1, groups2F));
            groupRepository.saveAll(groupsF);

            final Device deviceAllFalse = Device.builder().name("device 2").groups(groupsF).marker(false).build();
            deviceRepository.save(deviceAllFalse);
        }

        // Device 3
        {
            final Device deviceAllFalse = Device.builder().name("without group").marker(false).build();
            deviceRepository.save(deviceAllFalse);
        }

//        groupRepository.findAll().forEach(System.out::println);
//        System.out.println();
//        System.out.println();
//        deviceRepository.findAll().forEach(x -> {
//            System.out.println();
//            System.out.println(x);
//        });
//        System.out.println();

    }


//    @Test
//    void test(){
//        deviceRepository.findAll(addJoinedTableInSelect())
//    }

    @Test
    public void test(){
        System.out.println("==========================================================================================");
        deviceService.setAccessMarker(Stream.of(Boolean.TRUE).collect(Collectors.toSet()));
        final DeviceSearchRequest build = DeviceSearchRequest.builder().build();

        final Page<Device> page1 = deviceService.searchProfilesPaged(build, PageRequest.of(0, 2));
        page1.forEach(x -> {
            System.out.println();
            System.out.println(x);
        });
        assertEquals(1, page1.getTotalElements());

        assertEquals(1, page1.getContent().size());
        assertEquals(2, page1.getContent().get(0).getGroups().size());



        final Page<Device> page2 = deviceService.searchProfilesPaged(build, PageRequest.of(1, 2));
        page2.forEach(x -> {
            System.out.println();
            System.out.println(x);
        });

        assertEquals(0, page2.getContent().size());
    }

    @Test
    public void test2(){
        System.out.println("==========================================================================================");
        deviceService.setAccessMarker(Stream.of(Boolean.FALSE).collect(Collectors.toSet()));

        System.out.println();
        System.out.println(groups6);
        System.out.println();

        final DeviceSearchRequest build = DeviceSearchRequest.builder().groupIds(new HashSet<>(Collections.singletonList(groups6.getId()))).build();

        final PageRequest pageable = PageRequest.of(0, 2, new Sort(Sort.Direction.DESC, "groups"));
//        final PageRequest pageable = PageRequest.of(0, 2);

        final Page<Device> page1 = deviceService.searchProfilesPaged(build, pageable);
        page1.forEach(x -> {
            System.out.println();
            System.out.println(x);
        });
        assertEquals(1, page1.getTotalElements());

        assertEquals(1, page1.getContent().size());
        assertEquals(1, page1.getContent().get(0).getGroups().size());



        final Page<Device> page2 = deviceService.searchProfilesPaged(build, PageRequest.of(1, 2));
        page2.forEach(x -> {
            System.out.println();
            System.out.println(x);
        });

        assertEquals(0, page2.getContent().size());

    }

    private static Specification<Device> addJoinedTableInSelect1() {
        return (Specification<Device>) (r, q, b) -> {
            List<Predicate> predicates = new ArrayList<>();

            q.distinct(true);
            final Join<Device, Groups> join = (Join<Device, Groups>) r.fetch(Device_.groups);
            predicates.add(b.equal(join.get(Groups_.marker), r.get(Device_.marker)));

            return b.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

    @Transactional
    void joinWithSort() {
        final Page<Device> all = deviceRepository.findAll(addJoinedTableInSelect(), PageRequest.of(0, 100, new Sort(Sort.Direction.DESC, "groups")));
        System.out.println();
        all.forEach(System.out::println);
        System.out.println();
    }

    @Transactional
    void simpleJoinRequest() {
        final List<Device> all = deviceRepository.findAll(simpleJoin());
        System.out.println();
        all.forEach(System.out::println);
        System.out.println();
    }

    @Transactional
    void addJoin() {
        final List<Device> all = deviceRepository.findAll(addJoinedTableInSelect());
        System.out.println();
        all.forEach(System.out::println);
        System.out.println();
    }


}
