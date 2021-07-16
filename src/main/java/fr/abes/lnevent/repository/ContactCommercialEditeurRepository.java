package fr.abes.lnevent.repository;

import fr.abes.lnevent.entities.ContactCommercialEditeurEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactCommercialEditeurRepository extends JpaRepository<ContactCommercialEditeurEntity, Long> {

    @Query(nativeQuery = true, value = "select case when exists(select * from ContactCommercialEditeurEntity "
            + "where mailContactCommercial = :mail) then 'true' else 'false' end from dual")
    Boolean existeMail(@Param("mail") String mail);

}
