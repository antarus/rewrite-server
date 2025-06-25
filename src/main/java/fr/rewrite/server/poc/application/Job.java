package fr.rewrite.server.poc.application;

import java.time.LocalDateTime;

//@Entity // Indique que c'est une entité JPA
//@Table(name = "job_rewrite") // Nom de la table dans la BDD (optionnel, par défaut le nom de la classe)

public class Job {

  public enum Status {
    QUEUED,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED,
  }

  //  @Id // Clé primaire
  private String id; // Garde String pour UUID

  //  @Column(columnDefinition = "TEXT") // Pour stocker le JSON comme texte long
  private String configJson; // Stocke la RewriteConfig sérialisée en JSON

  //  @Enumerated(EnumType.STRING) // Stocke l'énumération comme une chaîne de caractères
  private Status status;

  private String currentStep;

  private LocalDateTime submittedAt;
  private LocalDateTime startedAt;
  private LocalDateTime completedAt;

  //  @Column(columnDefinition = "TEXT") // Pour les messages d'erreur et logs, qui peuvent être longs
  private String errorMessage;

  //  @Column(columnDefinition = "TEXT")
  private String outputLogs;

  private String resultUrl;

  private String repoUrl;
  private String recipeName;

  // Constructeur simplifié pour la création initiale
  public Job(String id, String configJson, String repoUrl, String recipeName) {
    this.id = id;
    this.configJson = configJson;
    this.status = Status.QUEUED;
    this.submittedAt = LocalDateTime.now();
    this.repoUrl = repoUrl;
    this.recipeName = recipeName;
  }
}
