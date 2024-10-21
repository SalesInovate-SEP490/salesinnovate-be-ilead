package fpt.capstone.SalesInnovate.iLead.model.filter;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="filter_store_users")
public class FilterStoreUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "filter_store_users_id")
    private Long filterStoreUsersId ;
    @Column(name = "user_id")
    private String userId;
    @Column(name = "filter_store_id")
    private Long filterStoreId;
}
