package core.services.management;

import core.models.Passport;
import core.services.core.ClientService;
import core.services.core.PassportService;
import core.services.core.TrainerService;

import java.util.List;


public class PassportManagementService {

    private final ClientService clientService;
    private final PassportService passportService;
    private final TrainerService trainerService;

    public PassportManagementService(ClientService clientService, PassportService passportService, TrainerService trainerService) {
        this.clientService = clientService;
        this.passportService = passportService;
        this.trainerService = trainerService;
    }

    public void deleteIfPassportNotOccupied(Passport passport){
        if (passportOccupiedByUsers(passport)){
            throw new IllegalArgumentException("Passport is in use");
        }
        passportService.deletePassport(passport);
    }

    public void createIfPassportNotOccupied(Passport passport){
        if (passportOccupied(passport)){
            throw new IllegalArgumentException("Passport is in use");
        }
        passportService.createPassport(passport);
    }

    public void updateIfPassportNotOccupied(Passport passport)
    {
        if (passportOccupied(passport, passport.getId())){
            throw new IllegalArgumentException("Passport series and number are in use");
        }
        passportService.updatePassport(passport);
    }

    public List<Passport> getAllFreePassports() {
        return passportService.getAllPassports().stream()
                .filter(passport -> !passportOccupiedByUsers(passport))
                .toList();
    }

    private boolean passportOccupiedByUsers(Passport passport){
        return trainerService.getAllTrainers().stream()
                .anyMatch(trainer -> trainer.getPassportId().equals(passport.getId()))
                ||
                clientService.getAllClients().stream()
                .anyMatch(client -> client.getPassportId().equals(passport.getId()));
    }

    private boolean passportOccupied(Passport passport){
        return passportService.getAllPassports().stream()
                .anyMatch(existingPassport ->
                        existingPassport.getSeries().equals(passport.getSeries()) &&
                        existingPassport.getNumber().equals(passport.getNumber())
                );
    }

    private boolean passportOccupied(Passport passport, String excludedId){
        return passportService.getAllPassports().stream()
                .anyMatch(existingPassport ->
                        !existingPassport.getId().equals(excludedId) &&
                         existingPassport.getSeries().equals(passport.getSeries()) &&
                         existingPassport.getNumber().equals(passport.getNumber())
                );
    }


}