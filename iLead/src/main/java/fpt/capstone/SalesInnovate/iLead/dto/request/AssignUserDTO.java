package fpt.capstone.SalesInnovate.iLead.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class AssignUserDTO {
    List<Long> leadIds;
    List<LeadsUserDTO> leadsUserDTOS;
}
