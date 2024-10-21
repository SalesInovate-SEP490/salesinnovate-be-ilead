package fpt.capstone.SalesInnovate.iLead.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name="leads_user")
public class LeadsUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leads_user_id")
    private Long leadsUserId;
    @Column(name = "lead_id")
    private Long leadId;
    @Column(name = "user_id")
    private String userId;
}
