package fpt.capstone.SalesInnovate.iLead.dto.request;

import jakarta.persistence.Column;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadsUserDTO {
    private Long leadsUserId;
    private Long leadId;
    private String userId;
}
