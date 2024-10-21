package fpt.capstone.SalesInnovate.iLead.repository;

import fpt.capstone.SalesInnovate.iLead.model.LeadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeadStatusRepository extends JpaRepository<LeadStatus,Long> {
    LeadStatus findLeadStatusByLeadStatusName(String leadStatusName);
}
