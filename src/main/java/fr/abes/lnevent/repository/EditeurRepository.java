package fr.abes.lnevent.repository;

import fr.abes.lnevent.entities.EditeurEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EditeurRepository extends JpaRepository<EditeurEntity, Long> {
    void deleteById(Long id);
    EditeurEntity getFirstByNom(String nom);
}
