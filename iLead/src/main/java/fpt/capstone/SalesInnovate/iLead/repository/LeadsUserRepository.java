package fpt.capstone.SalesInnovate.iLead.repository;

import fpt.capstone.SalesInnovate.iLead.model.LeadsUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface LeadsUserRepository extends JpaRepository<LeadsUser,Long>,JpaSpecificationExecutor<LeadsUser> {
}
