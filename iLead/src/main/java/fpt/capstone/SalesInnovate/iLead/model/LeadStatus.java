package fpt.capstone.SalesInnovate.iLead.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="lead_status")
@Builder
public class LeadStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lead_status_id")
    private Long leadStatusId ;
    @Column(name = "lead_status_name")
    private String leadStatusName;
    @Column(name="lead_status_index")
    private Integer leadStatusIndex;
}
