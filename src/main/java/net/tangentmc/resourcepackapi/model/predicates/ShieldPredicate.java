package net.tangentmc.resourcepackapi.model.predicates;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShieldPredicate extends DamagePredicate {
    int blocking;

    public ShieldPredicate(int damaged, double damage, int blocking) {
        super(damaged, damage);
        this.blocking = blocking;
    }
}
