package core.services.storage;

import core.models.Subscription;
import core.services.base.JsonStorageService;

public class SubscriptionStorageService extends JsonStorageService<Subscription> {
    public SubscriptionStorageService() {
        super(Subscription.class, "files/subscriptions");
    }

    @Override
    protected String getId(Subscription subscription) {
        return subscription.getId();
    }
}