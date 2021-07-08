package fr.abes.lnevent.controllers;

import fr.abes.lnevent.dto.etablissement.*;
import fr.abes.lnevent.entities.EtablissementEntity;
import fr.abes.lnevent.entities.IpEntity;
import fr.abes.lnevent.event.etablissement.*;
import fr.abes.lnevent.entities.EventEntity;
import fr.abes.lnevent.exception.AccesInterditException;
import fr.abes.lnevent.exception.SirenIntrouvableException;
import fr.abes.lnevent.recaptcha.ReCaptchaResponse;
import fr.abes.lnevent.repository.ContactRepository;
import fr.abes.lnevent.repository.EtablissementRepository;
import fr.abes.lnevent.repository.EventRepository;
import fr.abes.lnevent.security.exception.DonneeIncoherenteBddException;
import fr.abes.lnevent.security.payload.response.JwtAuthenticationResponse;
import fr.abes.lnevent.security.services.FiltrerAccesServices;
import fr.abes.lnevent.security.services.impl.UserDetailsImpl;
import fr.abes.lnevent.security.services.impl.UserDetailsServiceImpl;
import fr.abes.lnevent.services.EmailService;
import fr.abes.lnevent.services.GenererIdAbes;
import fr.abes.lnevent.services.ReCaptchaService;
import lombok.extern.slf4j.Slf4j;
import org.h2.engine.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Slf4j
@RestController
@RequestMapping("/v1/ln/etablissement")
public class EtablissementController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EtablissementRepository etablissementRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private  ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private GenererIdAbes genererIdAbes;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ReCaptchaService reCaptchaService;

    @Autowired
    FiltrerAccesServices filtrerAccesServices;

    @Autowired
    public JavaMailSender mailSender;

    @Autowired
    EmailService emailService;

    @Value("${ln.dest.notif.admin}")
    private String admin;


    @PostMapping("/creationCompte")
    public ResponseEntity<?> creationCompte(HttpServletRequest request, @Valid @RequestBody EtablissementCreeDTO eventDTO) {
        log.debug("eventDto = " + eventDTO.toString());
        log.debug("NomEtab = " + eventDTO.getNom());
        log.debug("siren = " + eventDTO.getSiren());
        log.debug("TypeEtablissement = " + eventDTO.getTypeEtablissement());
        log.debug("NomContact = " + eventDTO.getNomContact());
        log.debug("PrenomContact = " + eventDTO.getPrenomContact());
        log.debug("AdresseContact = " + eventDTO.getAdresseContact());
        log.debug("BPContact = " + eventDTO.getBoitePostaleContact());
        log.debug("CodePostalContact = " + eventDTO.getCodePostalContact());
        log.debug("VilleContact = " + eventDTO.getVilleContact());
        log.debug("CedexContact = " + eventDTO.getCedexContact());
        log.debug("TelephoneContact = " + eventDTO.getTelephoneContact());
        log.debug("MailContact = " + eventDTO.getMailContact());
        log.debug("mdp = " + eventDTO.getMotDePasse());
        log.debug("recaptcharesponse = " + eventDTO.getRecaptcha());

        String recaptcharesponse = eventDTO.getRecaptcha();
        String action = "creationCompte";

        //verifier la réponse recaptcha
        ReCaptchaResponse reCaptchaResponse = reCaptchaService.verify(recaptcharesponse, action);
        if(!reCaptchaResponse.isSuccess()){
            return ResponseEntity
                    .badRequest()
                    .body("Erreur ReCaptcha : " +  reCaptchaResponse.getErrors());
        }

        //verifier que le siren n'est pas déjà en base
        boolean existeSiren = etablissementRepository.existeSiren(eventDTO.getSiren());
        log.info("existeSiren = "+ existeSiren);
        if (existeSiren) {
            return ResponseEntity
                    .badRequest()
                    .body("Cet établissement existe déjà.");
        }
        //verifier que le mail du contact n'est pas déjà en base
        //boolean existeMail = etablissementRepository.findEtablissementEntityByContactContains(eventDTO.getMailContact()));
        boolean existeMail = contactRepository.findContactEntityByMail(eventDTO.getMailContact())!=null;
        log.info("existeMail = "+ existeMail);
        if (existeMail) {
            return ResponseEntity
                    .badRequest()
                    .body("L'adresse mail renseignée est déjà utilisée. Veuillez renseigner une autre adresse mail.");
        }
        //on crypte le mot de passe + on génère un idAbes + on déclenche la méthode add du controlleur etab
        else{
            log.info("mdp = " + eventDTO.getMotDePasse());
            eventDTO.setMotDePasse(passwordEncoder.encode(eventDTO.getMotDePasse()));
            eventDTO.setIdAbes(genererIdAbes.genererIdAbes(GenererIdAbes.generateId()));
            eventDTO.setRoleContact("etab");
            log.info("idAbes = " + eventDTO.getIdAbes());
            log.info("mdphash = " + eventDTO.getMotDePasse());
            EtablissementCreeEvent etablissementCreeEvent =
                    new EtablissementCreeEvent(this,
                            eventDTO);
            applicationEventPublisher.publishEvent(etablissementCreeEvent);
            eventRepository.save(new EventEntity(etablissementCreeEvent));
            String emailUser = eventDTO.getMailContact();
            emailService.constructCreationCompteEmailUser( request.getLocale(), emailUser);
            emailService.constructCreationCompteEmailAdmin( request.getLocale(), admin, eventDTO.getSiren(), eventDTO.getNom());
            return ResponseEntity.ok("Creation du compte effectuée.");}
    }

   /* public String add(@RequestBody EtablissementCreeDTO eventDTO) {
        EtablissementCreeEvent etablissementCreeEvent =
                new EtablissementCreeEvent(this,
                        eventDTO);
        applicationEventPublisher.publishEvent(etablissementCreeEvent);
        eventRepository.save(new EventEntity(etablissementCreeEvent));

        return "done";
    }*/

    @PostMapping(value = "/modification")
    public String edit(@Valid @RequestBody EtablissementModifieDTO eventDTO) throws SirenIntrouvableException, AccesInterditException{
        log.info("debut EtablissementController modification");
        EtablissementModifieEvent etablissementModifieEvent =
                new EtablissementModifieEvent(this,
                        filtrerAccesServices.getSirenFromSecurityContextUser(),
                        eventDTO.getNomContact(),
                        eventDTO.getPrenomContact(),
                        eventDTO.getMailContact(),
                        eventDTO.getTelephoneContact(),
                        eventDTO.getAdresseContact(),
                        eventDTO.getBoitePostaleContact(),
                        eventDTO.getCodePostalContact(),
                        eventDTO.getVilleContact(),
                        eventDTO.getCedexContact());
        applicationEventPublisher.publishEvent(etablissementModifieEvent);
        eventRepository.save(new EventEntity(etablissementModifieEvent));

        return "done";
    }

    @PostMapping(value = "/fusion")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<String> fusion(@RequestBody EtablissementFusionneDTO eventDTO) {
        try{
        EtablissementFusionneEvent etablissementFusionneEvent
                = new EtablissementFusionneEvent(this, eventDTO.getEtablissementDTO(), eventDTO.getSirenFusionnes());
        applicationEventPublisher.publishEvent(etablissementFusionneEvent);
        eventRepository.save(new EventEntity(etablissementFusionneEvent));
        return ResponseEntity.ok("Fusion effectuée.");

        } catch(Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Erreur lors de la fusion. Veuillez vérifier les SIREN et les informations renseignées.");
        }

    }

    @PostMapping(value = "/division")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<String> division(@RequestBody EtablissementDiviseDTO eventDTO) {
        try{
        EtablissementDiviseEvent etablissementDiviseEvent
                = new EtablissementDiviseEvent(this, eventDTO.getAncienSiren(), eventDTO.getEtablissementDTOS());
        applicationEventPublisher.publishEvent(etablissementDiviseEvent);
        eventRepository.save(new EventEntity(etablissementDiviseEvent));
        return ResponseEntity.ok("Scission effectuée.");

        } catch(Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Erreur lors de la scission. Veuillez vérifier les SIREN et les informations renseignées");
        }
    }

    @PostMapping(value = "/suppression/{siren}")
    @PreAuthorize("hasAuthority('admin')")
    public String suppression(HttpServletRequest request,  @PathVariable String siren, @RequestBody Map<String, String> motif) throws DonneeIncoherenteBddException {
        //envoi du mail de suppression
        EtablissementEntity etab = etablissementRepository.getFirstBySiren(siren);
        UserDetails user = new UserDetailsServiceImpl().loadUser(etab);
        String emailUser = ((UserDetailsImpl) user).getEmail();
        String nomEtab = ((UserDetailsImpl) user).getNameEtab();
        emailService.constructSuppressionMail( request.getLocale(), motif.get("motif"), nomEtab, emailUser);

        EtablissementSupprimeEvent etablissementSupprimeEvent
                = new EtablissementSupprimeEvent(this, siren);
        applicationEventPublisher.publishEvent(etablissementSupprimeEvent);
        eventRepository.save(new EventEntity(etablissementSupprimeEvent));

        return "done";
    }

    @GetMapping(value = "/{siren}")
    @PreAuthorize("hasAuthority('admin')")
    public EtablissementEntity get(@PathVariable String siren) {
        return etablissementRepository.getFirstBySiren(siren);
    }

    @GetMapping(value = "/getDerniereDateModificationIp/{siren}")
    @PreAuthorize("hasAuthority('admin')")
    public String getDerniereDateModificationIp(@PathVariable String siren) {
        ArrayList<String> listDateModifIp = etablissementRepository.findDateModificationBySiren(siren);
        ArrayList<String> listDateModifCourtes = new ArrayList<>();
        for(String date:listDateModifIp) listDateModifCourtes.add(date.substring(0, 10));
        log.info("dtaModif = "+ listDateModifCourtes.get(0));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Collections.sort(listDateModifCourtes, Comparator.comparing(s -> LocalDate.parse(s, formatter).atStartOfDay()));
        log.info("dtaModif = "+ listDateModifCourtes.get(0));
        log.info("dtaModif = "+ listDateModifCourtes.get(listDateModifCourtes.size() - 1));

        for(String date:listDateModifCourtes) log.info(date);
        return listDateModifCourtes.get(listDateModifCourtes.size() - 1);
    }

    @GetMapping(value = "/getInfoEtab")
    public EtablissementEntity getInfoEtab() throws SirenIntrouvableException, AccesInterditException {
        return etablissementRepository.getFirstBySiren(filtrerAccesServices.getSirenFromSecurityContextUser());
    }

    @GetMapping(value = "/getListEtab")
    @PreAuthorize("hasAuthority('admin')")
    public List<EtablissementEntity> getListEtab() {
        return etablissementRepository.findAll();
    }

    /*private String getSirenFromSecurityContextUser() throws SirenIntrouvableException, AccesInterditException{
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String sirenFromSecurityContextUser = userDetails.getUsername();
        log.info("sirenFromSecurityContextUser = " + sirenFromSecurityContextUser);
        if(sirenFromSecurityContextUser.equals("") || sirenFromSecurityContextUser==null){
            log.error("Acces interdit");
            throw new AccesInterditException("Acces interdit");
        }
        boolean existeSiren = etablissementRepository.existeSiren(sirenFromSecurityContextUser);
        log.info("existeSiren = "+ existeSiren);
        if (!existeSiren) {
            log.error("Siren absent de la base");
            throw new SirenIntrouvableException("Siren absent de la base");
        }
        return sirenFromSecurityContextUser;
    }*/
}
