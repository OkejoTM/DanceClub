package core.services.core;

import core.models.Passport;
import core.services.storage.PassportStorageService;

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

    public void deletePassport(Passport passport)
    {
        passportStorageService.delete(passport.getId());
    }

    public void updatePassport(Passport passport)
    {
        passportStorageService.save(passport);
    }

    public void createPassport(Passport passport)
    {
        passportStorageService.save(passport);
    }

}
