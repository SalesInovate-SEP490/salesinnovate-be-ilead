package fpt.capstone.SalesInnovate.iLead.repository;

import fpt.capstone.SalesInnovate.iLead.model.LeadSalution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeadSalutionRepository extends JpaRepository<LeadSalution,Long> {
    LeadSalution findLeadSalutionByLeadSalutionName(String leadSalutionName);
}
