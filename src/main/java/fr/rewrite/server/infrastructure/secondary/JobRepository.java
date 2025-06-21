package fr.rewrite.server.infrastructure.secondary;

import fr.rewrite.server.domain.Job;
//import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface JobRepository /*extends JpaRepository<Job, String> */{
  // Spring Data JPA fournit déjà findById, save, etc.
  // Vous pouvez ajouter des méthodes personnalisées ici si besoin, ex:
  // List<Job> findByStatus(Job.Status status);
}
