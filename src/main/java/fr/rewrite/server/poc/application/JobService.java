package fr.rewrite.server.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.rewrite.server.application.dto.RewriteConfig;
import fr.rewrite.server.domain.Job;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobService {

  //  private final JobRepository jobRepository; // Injection du repository JPA
  private final ObjectMapper objectMapper;

  public JobService(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Transactional // S'assure que l'opération est atomique
  public Job createJob(RewriteConfig config) throws JsonProcessingException {
    //    String jobId = UUID.randomUUID().toString();
    //    String configJson = objectMapper.writeValueAsString(config);
    //
    //    Job job = new Job(jobId, configJson, config.repoUrl(), config.recipeName());
    //    job.setStatus(Job.Status.QUEUED);
    //    job.setSubmittedAt(LocalDateTime.now()); // S'assurer que le submittedAt est défini lors de la création
    //
    //    return jobRepository.save(job); // Sauvegarde dans PostgreSQL
    return null;
  }

  @Transactional
  public void updateJobStatus(String jobId, Job.Status status, String currentStep, String errorMessage, String resultUrl) {
    //    jobRepository.findById(jobId).ifPresent(job -> {
    //      job.setStatus(status);
    //      if (currentStep != null) {
    //        job.setCurrentStep(currentStep);
    //      }
    //      if (errorMessage != null) {
    //        job.setErrorMessage(errorMessage);
    //      }
    //      if (resultUrl != null) {
    //        job.setResultUrl(resultUrl);
    //      }
    //      if (status == Job.Status.COMPLETED || status == Job.Status.FAILED || status == Job.Status.CANCELLED) {
    //        job.setCompletedAt(LocalDateTime.now());
    //      } else if (status == Job.Status.RUNNING && job.getStartedAt() == null) {
    //        job.setStartedAt(LocalDateTime.now());
    //      }
    //      jobRepository.save(job); // Sauvegarde les modifications
    //    });
  }

  @Transactional
  public void appendJobLog(String jobId, String logMessage) {
    //    jobRepository.findById(jobId).ifPresent(job -> {
    //      job.setOutputLogs(Optional.ofNullable(job.getOutputLogs()).orElse("") + logMessage);
    //      jobRepository.save(job); // Sauvegarde les modifications
    //    });
  }

  public Optional<Job> getJobById(String jobId) {
    //    return jobRepository.findById(jobId); // Récupère le job depuis PostgreSQL
    return null;
  }
}
