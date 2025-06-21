package fr.rewrite.server.infrastructure.secondary;

public interface JobRepository /*extends JpaRepository<Job, String> */{
  // Spring Data JPA fournit déjà findById, save, etc.
  // Vous pouvez ajouter des méthodes personnalisées ici si besoin, ex:
  // List<Job> findByStatus(Job.Status status);
}
