package ru.zaets.home.research.criteriaapi.two;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import java.text.MessageFormat;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table
public class Device {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator",
            parameters = {
                    @Parameter(
                            name = "uuid_gen_strategy_class",
                            value = "org.hibernate.id.uuid.CustomVersionOneStrategy"
                    )
            }
    )
    @Column(name = "id")
    private UUID id;

    @Column(name = "name")
    private String name;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @JoinTable(
            name = "device_to_group_link",
            joinColumns = @JoinColumn(name = "device_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "group_id", referencedColumnName = "id")
    )
    @BatchSize(size = 100)
    private Set<Groups> groups;

    @Column
    private boolean marker;

    @Override
    public String toString() {
        return MessageFormat.format("Device'{" +
                "\n\t'id={0}, " +
                "\n\tname=''{1}''," +
                "\n\tgroups={2}, " +
                "\n\tmarker={3}'" +
                "\n}'", id, name, groups, marker);
    }
}
