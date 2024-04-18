package gr.grnet.pccapi.service;

import gr.grnet.pccapi.dto.PageResource;
import gr.grnet.pccapi.dto.PartialPrefixDto;
import gr.grnet.pccapi.dto.PrefixDto;
import gr.grnet.pccapi.dto.PrefixResponseDto;
import gr.grnet.pccapi.entity.Domain;
import gr.grnet.pccapi.entity.Prefix;
import gr.grnet.pccapi.entity.Provider;
import gr.grnet.pccapi.entity.Service;
import gr.grnet.pccapi.exception.ConflictException;
import gr.grnet.pccapi.mapper.PrefixMapper;
import gr.grnet.pccapi.repository.DomainRepository;
import gr.grnet.pccapi.repository.PrefixRepository;
import gr.grnet.pccapi.repository.ProviderRepository;
import gr.grnet.pccapi.repository.ServiceRepository;
import gr.grnet.pccapi.repository.StatisticsRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.UriInfo;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class PrefixService {
  @Inject DomainRepository domainRepository;
  @Inject ProviderRepository providerRepository;
  @Inject ServiceRepository serviceRepository;
  @Inject PrefixRepository prefixRepository;
  // Logger logger;
  @Inject StatisticsRepository statisticsRepository;

  @ConfigProperty(name = "daemon.jar.path")
  String daemonJarPath;

  @ConfigProperty(name = "daemon.log.path")
  String daemonLogPath;

  private static final Logger logger = Logger.getLogger(PrefixService.class);

  /**
   * Creates a new prefix based on the provided arguments, runs validation checks and returns the
   * appropriate response dto
   */
  @Transactional
  public PrefixResponseDto create(PrefixDto prefixDto) {

    logger.info("Inserting new prefix . . .");

    // check the uniqueness of the provided name
    if (prefixRepository.existsByName(prefixDto.getName())) {
      throw new ConflictException("Prefix name already exists");
    }
    // check the existence of the provided provider
    Provider provider =
        providerRepository
            .findByIdOptional(prefixDto.getProviderId())
            .orElseThrow(() -> new NotFoundException("Provider not found"));

    var lookUpServiceType =
        PrefixMapper.INSTANCE.validateLookUpServiceType(prefixDto.lookUpServiceType);
    prefixDto.lookUpServiceType = String.valueOf(lookUpServiceType);

    var contractType = PrefixMapper.INSTANCE.validateContractType(prefixDto.contractType);
    prefixDto.contractType = String.valueOf(contractType);

    Prefix prefix = PrefixMapper.INSTANCE.requestToPrefix(prefixDto);
    if (prefixDto.serviceId != null) {
      // check the existence of the provided service
      Service service =
          serviceRepository
              .findByIdOptional(prefixDto.getServiceId())
              .orElseThrow(() -> new NotFoundException("Service not found"));
      prefix.setService(service);
    }
    // check the existence of the provided domain
    if (prefixDto.domainId != null) {
      Domain domain =
          domainRepository
              .findByIdOptional(prefixDto.getDomainId())
              .orElseThrow(() -> new NotFoundException("Domain not found"));
      prefix.setDomain(domain);
    }
    prefix.setProvider(provider);
    prefixRepository.persist(prefix);
    try {
      resolvePrefixes(prefix.name);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return PrefixMapper.INSTANCE.prefixToResponseDto(prefix);
  }

  public PageResource<PrefixResponseDto> fetchByPageAndSize(int page, int size, UriInfo uriInfo) {

    var prefixes = prefixRepository.fetchPrefixesByPage(page, size);
    return new PageResource<>(
        prefixes, PrefixMapper.INSTANCE.prefixesToResponseDto(prefixes.list()), uriInfo);
  }

  /**
   * Returns a Prefix by the given ID
   *
   * @return The stored Prefix has been turned into a response body.
   */
  public PrefixResponseDto fetchById(Integer id) {

    logger.infof("Fetching the Prefix with ID : %s", id);

    var prefix =
        prefixRepository
            .findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Prefix not found"));

    return PrefixMapper.INSTANCE.prefixToResponseDto(prefix);
  }

  @Transactional
  public PrefixResponseDto patchById(int id, PartialPrefixDto prefixDto) {

    logger.info("Partially updating existing prefix . . .");
    Prefix prefix =
        prefixRepository
            .findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Prefix not found"));

    // check the uniqueness of the provided name
    if (prefixRepository.existsByName(prefixDto.getName())) {
      Prefix prefixByName = prefixRepository.findByName(prefixDto.getName());
      if (prefixByName != null && prefixByName.id != id) {
        throw new ConflictException("Prefix name already exists");
      }
    }

    if (prefixDto.status != null) {
      prefix.status = Integer.valueOf(prefixDto.status);
    }

    PrefixMapper.INSTANCE.updatePrefixFromDto(prefixDto, prefix);
    // check the existence of the provided provider and update entity on success
    if (prefixDto.getProviderId() != null) {
      Provider provider =
          providerRepository
              .findByIdOptional(prefixDto.getProviderId())
              .orElseThrow(() -> new NotFoundException("Provider not found"));
      prefix.setProvider(provider);
    }

    // check the existence of the provided service and update entity on success

    if (prefixDto.getServiceId() != null) {
      Service service =
          serviceRepository
              .findByIdOptional(prefixDto.getServiceId())
              .orElseThrow(() -> new NotFoundException("Service not found"));

      prefix.setService(service);
    }

    // check the existence of the provided domain and update entity on success

    if (prefixDto.getDomainId() != null) {
      Domain domain =
          domainRepository
              .findByIdOptional(prefixDto.getDomainId())
              .orElseThrow(() -> new NotFoundException("Domain not found"));
      prefix.setDomain(domain);
    }
    return PrefixMapper.INSTANCE.prefixToResponseDto(prefix);
  }

  /**
   * This method delegates a prefix deletion query to {@link PrefixRepository prefixRepository}.
   *
   * @param id The prefix ID to be deleted
   */
  @Transactional
  public void deleteById(Integer id) {
    boolean deleted = prefixRepository.deleteById(id);
    if (!deleted) {
      throw new NotFoundException("Prefix not found");
    }
  }

  /**
   * Full update of a prefix on all attributes
   *
   * @param prefixDto contains the changes that will be applied
   * @param id the id of prefix to be updated
   * @return PrefixResponseDto
   */
  @Transactional
  public PrefixResponseDto update(PrefixDto prefixDto, int id) {

    logger.info("Full updating a  prefix . . .");
    // check the existence of the provided service
    Prefix prefix =
        prefixRepository
            .findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Prefix not found"));

    // check the existence of the provided provider
    Provider provider =
        providerRepository
            .findByIdOptional(prefixDto.getProviderId())
            .orElseThrow(() -> new NotFoundException("Provider not found"));

    // check the uniqueness of the provided name
    if (prefixRepository.existsByName(prefixDto.getName())) {
      Prefix prefixByName = prefixRepository.findByName(prefixDto.getName());
      if (prefixByName != null && prefixByName.id != id) {
        throw new ConflictException("Prefix name already exists");
      }
    }
    var lookUpServiceType =
        PrefixMapper.INSTANCE.validateLookUpServiceType(prefixDto.lookUpServiceType);
    prefixDto.lookUpServiceType = String.valueOf(lookUpServiceType);

    var contractType = PrefixMapper.INSTANCE.validateContractType(prefixDto.contractType);
    prefixDto.contractType = String.valueOf(contractType);

    PrefixMapper.INSTANCE.updateRequestToPrefix(prefixDto, prefix);
    Service service = null;
    if (prefixDto.serviceId != null) {
      service =
          serviceRepository
              .findByIdOptional(prefixDto.getServiceId())
              .orElseThrow(() -> new NotFoundException("Service not found"));
    }
    prefix.setService(service);

    // check the existence of the provided domain
    Domain domain = null;

    if (prefixDto.domainId != null) {
      domain =
          domainRepository
              .findByIdOptional(prefixDto.getDomainId())
              .orElseThrow(() -> new NotFoundException("Domain not found"));
    }
    prefix.setDomain(domain);

    // update the prefix
    prefix.setProvider(provider);

    return PrefixMapper.INSTANCE.prefixToResponseDto(prefix);
  }

  private void executePIDResolveProcess(String prefixNum) {
    Process proc = null;
    try {
      if (Files.exists(Paths.get(daemonLogPath))) {
        System.out.println("log exists ");
        proc =
            Runtime.getRuntime()
                .exec(
                    "java -jar "
                        + daemonJarPath
                        + " -e 1 -m HEAD -N 54 -s 10 -p "
                        + prefixNum
                        + " -v FINE -T 1 -l "
                        + daemonLogPath);
      } else {
        System.out.println("log does not exists");

        proc =
            Runtime.getRuntime()
                .exec(
                    "java -jar "
                        + daemonJarPath
                        + " -e 1 -m HEAD -N 54 -s 10 -p "
                        + prefixNum
                        + " -v FINE -T 1 ");
      }
      System.out.println("is alive process " + proc.isAlive());

      // Thread.sleep(60000);
      synchronized (proc) {
        try {
          Thread.sleep(60000);
          proc.wait();
          System.out.println("am i waiting or not?");
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void resolvePrefixes(String prefixNum) throws SQLException {
    System.out.println("Thread in exec command " + Thread.currentThread().getName());
    if (!Files.exists(Paths.get(daemonJarPath))) {
      return;
    }
    statisticsRepository.insertPrefixStatistics(prefixNum, 0, 0, 0, 0);
    CustomCompletableFuture.runAsync(() -> executePIDResolveProcess(prefixNum))
        .thenRun(() -> statisticsRepository.executeUpdateResolvablePerPrefixProc(prefixNum));
  }

  public static class CustomCompletableFuture<T> extends CompletableFuture<T> {
    static final Executor EXEC = Executors.newCachedThreadPool();

    @Override
    public Executor defaultExecutor() {
      return EXEC;
    }

    @Override
    public <U> CompletableFuture<U> newIncompleteFuture() {
      return new CustomCompletableFuture<>();
    }

    public static CompletableFuture<Void> runAsync(Runnable runnable) {
      return supplyAsync(
          () -> {
            runnable.run();
            return null;
          });
    }

    public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier) {
      return new CompletableFuture<U>().completeAsync(supplier);
    }
  }
}
