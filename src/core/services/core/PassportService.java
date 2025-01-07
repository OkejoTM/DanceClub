package core.services.core;

import core.models.Passport;
import core.services.storage.PassportStorageService;

import java.time.LocalDate;
import java.util.List;

public class PassportService {
    private final PassportStorageService passportStorageService;

    public PassportService(PassportStorageService passportStorageService) {
        this.passportStorageService = passportStorageService;
    }


    public Passport getPassportById(String id)
    {
        return passportStorageService.getById(id);
    }

    public List<Passport> getAllPassports()
    {
        return passportStorageService.getAll();
    }

    public void deletePassport(String id)
    {
        passportStorageService.delete(id);
    }

    public void updatePassport(Passport passport)
    {
        passportStorageService.save(passport);
    }

    public Passport createPassport(String address, LocalDate birthDate, String number, String series, LocalDate overdue){
        Passport passport = new Passport(address, birthDate, number, series, overdue);
        passportStorageService.save(passport);
        return passport;
    }
    

}
