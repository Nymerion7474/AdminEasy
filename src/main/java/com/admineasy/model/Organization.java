// src/main/java/com/admineasy/model/Organization.java
package com.admineasy.model;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "organizations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"users", "contracts"}) // ← n’inclut pas ces collections dans toString()
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;  // ex : “ACME SARL”

    @Column(nullable = false)
    private String adminEmail;

    private boolean reminder2Months = false;
    private boolean reminder1Month = false;
    private boolean reminder2Weeks = false;
    private boolean reminder1Week = false;

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<User> users;

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Contract> contracts;
}
