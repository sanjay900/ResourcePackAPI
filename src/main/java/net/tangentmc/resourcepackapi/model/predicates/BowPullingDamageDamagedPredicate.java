package net.tangentmc.resourcepackapi.model.predicates;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BowPullingDamageDamagedPredicate extends BowPullingDamagePredicate {
    int damaged;
    public BowPullingDamageDamagedPredicate(int pulling, double damage, int damaged) {
        super(pulling, damage);
        this.damaged = damaged;
    }
}
