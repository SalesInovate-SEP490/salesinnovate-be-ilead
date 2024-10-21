package fpt.capstone.SalesInnovate.iLead.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="leads")
public class Leads {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lead_id")
    private Long leadId;
    @Column(name = "user_id")
    private String userId;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "middle_name")
    private String middleName;
    @Column(name = "gender")
    private Integer gender;
    @Column(name = "title")
    private String title;
    @Column(name = "email")
    private String email;
    @Column(name = "phone")
    private String phone;
    @Column(name = "website")
    private String website;
    @Column(name = "company")
    private String company;
    @Column(name = "no_employee")
    private Integer noEmployee;
    @Column(name = "created_by")
    private String createdBy;
    @Column(name = "create_date")
    private LocalDateTime createDate;
    @Column(name = "edit_date")
    private LocalDateTime editDate;
    @Column(name = "edit_by")
    private String editBy;
    @Column(name = "is_deleted")
    private Integer isDelete ;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "industry_id")
    private Industry industry;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "lead_source_id")
    private LeadSource leadSource;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "status_id")
    private LeadStatus leadStatus;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "address_information_id")
    private AddressInformation addressInformation;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "lead_rating_id")
    private LeadRating leadRating;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "lead_salution_id")
    private LeadSalution leadSalution;

    @ManyToMany(fetch = FetchType.LAZY,
            cascade = {CascadeType.DETACH, CascadeType.MERGE,
                    CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(name = "leads_user",
            joinColumns = @JoinColumn(name = "lead_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    private List<Users> users ;
}
