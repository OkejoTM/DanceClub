package core.services.storage;

import core.models.Passport;
import core.services.base.JsonStorageService;

public class PassportStorageService extends JsonStorageService<Passport> {
    public PassportStorageService() {
        super(Passport.class, "files/passports");
    }

    @Override
    protected String getId(Passport passport) {
        return passport.getId();
    }
}
